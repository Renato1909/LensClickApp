import { spawnSync } from 'node:child_process';
import { existsSync, mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const androidAssets = fileURLToPath(new URL('../app/src/main/res/drawable-nodpi/', import.meta.url));
const [webDirectory, persistFlag, persistDirectory, ...extra] = process.argv.slice(2);
if (!webDirectory || extra.length || (persistFlag && (persistFlag !== '--persist-to' || !persistDirectory))) {
  console.error('Uso: node tools/seed-feira-local.mjs /caminho/para/LensClick-main [--persist-to /caminho/dos/dados-locais]');
  process.exit(1);
}
const web = resolve(webDirectory);
const wrangler = join(web, 'node_modules', 'wrangler', 'bin', 'wrangler.js');
if (!existsSync(wrangler) || !existsSync(join(web, 'wrangler.jsonc'))) {
  console.error('Indique a pasta do LensClick web com as dependências instaladas.');
  process.exit(1);
}

const photographers = JSON.parse(readFileSync(
  new URL('../app/src/main/assets/feira/photographers.json', import.meta.url), 'utf8'
));

function localWrangler(...args) {
  const persistence = persistDirectory ? ['--persist-to', resolve(persistDirectory)] : [];
  const result = spawnSync(process.execPath, [wrangler, ...args, '--local', ...persistence], {
    cwd: web,
    env: { ...process.env, WRANGLER_SEND_METRICS: 'false' },
    stdio: 'inherit'
  });
  if (result.error) throw result.error;
  if (result.status !== 0) throw new Error(`Wrangler falhou em ${args.slice(0, 2).join(' ')}.`);
}

const quote = value => `'${value.replaceAll("'", "''")}'`;
const passwordMarker = 'feira-local-sem-login';
const directory = mkdtempSync(join(tmpdir(), 'lensclick-feira-'));
try {
  const sql = [];
  for (const photographer of photographers) {
    const email = quote(`${photographer.slug}@example.invalid`);
    const demoUser = `email = ${email} AND password_hash = ${quote(passwordMarker)} AND role = 'photographer'`;
    sql.push(`INSERT OR IGNORE INTO users (name, email, phone, password_hash, role)
      VALUES (${quote(photographer.name)}, ${email}, '', ${quote(passwordMarker)}, 'photographer');`);
    sql.push(`INSERT OR IGNORE INTO professional_profiles
      (user_id, slug, bio, specialties, city, state, service_radius_km, starting_price_cents, is_published)
      SELECT id, ${quote(photographer.slug)}, ${quote(`Perfil fictício para demonstração. ${photographer.bio}`)},
        ${quote(JSON.stringify(photographer.specialties))}, ${quote(photographer.city)}, 'SP',
        ${photographer.radius}, ${photographer.price}, 1
      FROM users WHERE ${demoUser};`);

    for (const [position, [file, caption, alt, category]] of photographer.photos.entries()) {
      const objectKey = `feira-local/${photographer.slug}/${file}`;
      localWrangler('r2', 'object', 'put', `lensclick-profile-images/${objectKey}`,
        `--file=${join(androidAssets, file)}`, '--content-type=image/jpeg');
      sql.push(`INSERT OR IGNORE INTO portfolio_items
        (user_id, object_key, caption, alt_text, position, is_cover, category)
        SELECT id, ${quote(objectKey)}, ${quote(caption)}, ${quote(alt)}, ${position},
          ${Number(position === 0)}, ${quote(category)}
        FROM users WHERE ${demoUser};`);
    }
  }
  const sqlFile = join(directory, 'fotografos.sql');
  writeFileSync(sqlFile, sql.join('\n'), { mode: 0o600 });
  localWrangler('d1', 'execute', 'lensclick-db', `--file=${sqlFile}`);
  console.log('Quatro fotógrafos de demonstração preparados no ambiente local.');
} finally {
  rmSync(directory, { recursive: true, force: true });
}
