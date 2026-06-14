# npm & Node.js Platform Engineering Notes

> Personal reference document — Node.js ecosystem in a DevOps context.

---

## Table of Contents

- [01 · Introduction](#01--introduction)
- [02 · npm Internals — How It Actually Works](#02--npm-internals--how-it-actually-works)
- [03 · package.json — Heart of a Node Project](#03--packagejson--heart-of-a-node-project)
- [04 · Dependency Management](#04--dependency-management)
- [05 · Semantic Versioning](#05--semantic-versioning)
- [06 · Lockfiles](#06--lockfiles)
- [07 · npm Cache](#07--npm-cache)
- [08 · node_modules Architecture](#08--node_modules-architecture)
- [09 · Workspaces & Monorepo Engineering](#09--workspaces--monorepo-engineering)
- [10 · Build Systems & Artifact Generation](#10--build-systems--artifact-generation)
- [11 · Testing & Quality Gates](#11--testing--quality-gates)
- [12 · Security & Supply Chain](#12--security--supply-chain)
- [13 · Containerization](#13--containerization)
- [14 · Operations & Observability](#14--operations--observability)
- [15 · The Full Lifecycle — Developer to Production](#15--the-full-lifecycle--developer-to-production)

---

## 01 · Introduction

### 1.1 Why npm Matters in DevOps

npm is the package manager for Node.js. In a DevOps context, it is responsible for turning developer source code into deployable artifacts. It handles dependency resolution, manages the build lifecycle, controls versioning, and feeds into CI/CD pipelines that ship applications to production.

A developer saying "my code is done" is the starting line for DevOps, not the finish line. From that moment, we are responsible for everything between source code and running service and npm is the engine behind nearly all of it in the Node.js world.

---

### 1.2 What I Aimed to Learn

My goal was to understand the complete journey of a Node.js application: from source files on a developer's laptop to a container running in production. I focused on how npm manages dependencies, how builds produce deployable artifacts, how quality gates prevent bad code from shipping, how containers make applications portable, and how observability tools make those containers understandable once deployed.

---

### 1.3 What This Document Covers

This document captures my hands-on experience across the full Node.js platform engineering stack. It is written the way I think — referencing real commands I ran, real errors I hit, and real explanations I had to work out for myself. The goal is a document I can share with another engineer learning this same path and have them walk away understanding the whole picture, end to end.

The learning path flows in one direction:

```
npm → Dependencies → Workspaces → Build Systems → Testing → Security → Containers → Observability → Production
```

This mirrors the actual order in which things get handed off in real organizations.

---

## 02 · npm Internals — How It Actually Works

### 2.1 What npm Is

npm stands for Node Package Manager. It ships bundled with Node.js and serves three purposes:

1. **Registry** — a public database at `registry.npmjs.org` containing hundreds of thousands of JavaScript packages
2. **CLI** — the command-line interface (`npm`) you interact with to install, update, publish, and manage packages
3. **Package format** — a standard for how packages are structured, versioned, and declared

---

### 2.2 Local vs Global Installs

Understanding where npm puts things is foundational. The two modes behave completely differently.

**Local install** (`npm install <package>`):

- npm looks in `~/.npm/_cacache` for a cached copy
- If found and valid (checksum matches), it unpacks from cache
- If not found, it hits the internet, downloads the tarball, caches it in `~/.npm`, then installs
- JavaScript code lands in `./node_modules/<package>`
- Executable binaries land in `./node_modules/.bin`

**Global install** (`npm install -g <package>`):

- Same cache check, same download flow
- JavaScript code lands in `~/.nvm/versions/node/<version>/lib/node_modules/<package>`
- Executables land in `~/.nvm/versions/node/<version>/bin/<package>`
- Available system-wide, not scoped to a project

The separation is intentional. Global packages are tools for the developer. Local packages are dependencies your application actually needs.

---

### 2.3 Configuration Inspection

These are the first commands to run when landing in a new environment:

```bash
npm config list       # Shows all active config + where it came from
npm config get cache  # /home/ubuntu/.npm — your tarball warehouse
npm config get prefix # ~/.nvm/versions/node/v24.16.0 — global install parent
npm root -g           # ~/.nvm/.../lib/node_modules — where global JS code lives
```

The **prefix** is the parent folder where global executables are stored. The **global root** is the specific directory inside that where the actual package JavaScript lives. The **cache** is where compressed tarballs sit, reused across both local and global installs.

---

### 2.4 npm Execution Flow (What Happens on `npm install`)

1. npm reads `package.json` to understand what's needed
2. Checks `package-lock.json` for exact versions (if it exists)
3. For each package: checks `~/.npm/_cacache` for a matching integrity hash
4. Cache hit → unpacks the tarball into `node_modules`
5. Cache miss → downloads from the registry, verifies checksum, caches it, then unpacks
6. Builds the dependency tree, hoists shared packages to root `node_modules`
7. Creates symlinks in `node_modules/.bin` for any executables
8. Updates `package-lock.json` if anything changed

The checksum verification step is not optional and not skippable. npm rejects packages whose hash doesn't match what the registry declared. This is supply chain security at the most basic level.

---

## 03 · package.json — Heart of a Node Project

### 3.1 Purpose

`package.json` is the identity and instruction manual of any Node.js project. It tells npm:

- what this package is (name, version, author, license)
- what it depends on to run
- what it needs during development
- how to run common tasks (scripts)
- what Node.js version it requires

Every project starts here. Running `npm init` (not `-y`) and reading what it asks is how you learn what each field actually means.

---

### 3.2 Anatomy of package.json

```json
{
  "name": "api-gateway",
  "version": "1.0.0",
  "description": "API gateway for the node-platform-lab",
  "license": "ISC",
  "author": "Kate Mwaura",
  "type": "commonjs",
  "main": "src/server.js",
  "scripts": {
    "start": "node src/server.js",
    "dev": "nodemon src/server.js",
    "build": "npm run clean && npx tsc",
    "clean": "rm -rf dist",
    "test": "jest",
    "lint": "eslint .",
    "format": "prettier --write ."
  },
  "dependencies": {
    "express": "^5.2.1",
    "dotenv": "^17.4.2",
    "winston": "^3.19.0",
    "joi": "^18.2.1"
  },
  "devDependencies": {
    "nodemon": "^3.1.14",
    "eslint": "^10.4.1",
    "prettier": "^3.8.3",
    "jest": "^29.0.0",
    "typescript": "^5.0.0"
  }
}
```

**Field breakdown:**

| Field | What it does |
|---|---|
| `name` | The package identity. Must be lowercase, no spaces. This is what other packages use to import you and what appears on the npm registry if you publish. |
| `version` | Follows semantic versioning (`MAJOR.MINOR.PATCH`). Starts at `1.0.0` by convention. npm uses this to track what's installed and resolve conflicts. |
| `description` | Human-readable summary. Shows up in registry search results and repository metadata. |
| `license` | Declares usage rights. `ISC` and `MIT` are permissive open-source licenses. Matters in enterprise — legal teams scan for non-compliant licenses in SBOMs. |
| `author` | Who owns and maintains this package. |
| `type` | Tells Node.js how to interpret `.js` files. `"commonjs"` means `require()` / `module.exports`. `"module"` means `import` / `export`. This must match how your code is written — getting it wrong causes cryptic runtime errors. |
| `main` | The entry point. When another package does `require('api-gateway')`, Node resolves to this file. In a built project this should point to `dist/server.js`, not `src/`. |

---

### 3.3 The Scripts Section

Scripts are shortcuts to commands. When you run `npm run build`, npm looks up `"build"` in the scripts section and executes whatever string is there. The reason this works with local binaries (like `jest` or `nodemon`) is PATH injection — npm temporarily adds `./node_modules/.bin` to `PATH` before executing the script, so locally installed tools are findable without global installs.

```bash
ls node_modules/.bin  # See every binary your local packages expose
```

This is why `npm run lint` can call `eslint` even though you never installed it globally.

---

### 3.4 Dependencies vs devDependencies

| | `dependencies` | `devDependencies` |
|---|---|---|
| Purpose | Required to run in production | Required during development only |
| Flag | `npm install <pkg>` | `npm install --save-dev <pkg>` |
| Included in production container | Yes | No |
| Examples | express, dotenv, winston | nodemon, jest, eslint, prettier |

The separation is not just organizational. When building Docker images, you run `npm install --omit=dev` to install only production dependencies. This keeps containers smaller and removes tools from the attack surface that production doesn't need.

---

### 3.5 Engines

```json
"engines": {
  "node": ">=20.0.0",
  "npm": ">=10.0.0"
}
```

Declares what versions of Node and npm this project is compatible with. Useful in CI pipelines to catch environment mismatches early.

---

## 04 · Dependency Management

### 4.1 Direct vs Transitive Dependencies

- **Direct dependencies** are what you explicitly listed in `package.json`. You made a deliberate choice to install them.
- **Transitive dependencies** are everything those packages need to run. You didn't install them — npm pulled them in automatically.

Running `npm ls` shows only your direct dependencies. Running `npm ls --all` reveals the full tree — every package, every level deep. The gap between those two outputs is always surprising. A project with 7 direct dependencies can have 1,166 total packages installed.

---

### 4.2 Dependency Resolution

When npm installs packages, it builds a graph of all dependencies and their dependencies. If two packages need the same dependency at compatible versions, npm hoists it — installs it once at the root `node_modules` and lets both share it. If versions conflict, npm nests a separate copy inside the package that needs the different version.

---

### 4.3 Inspecting the Dependency Graph

```bash
npm ls                  # Direct dependencies only
npm ls --all            # Full recursive tree
npm ls lodash           # Where a specific package is in the tree
npm explain lodash      # Why is this package installed? Who required it?
```

`npm explain` is the command to reach for when you see a package in `node_modules` and don't know why it's there. It traces the chain back to whoever required it.

---

### 4.4 Peer Dependencies

A peer dependency means: "I don't bundle my own copy of this — I expect the project using me to provide it."

The classic example is React component libraries. If you install `react-dom@18`, it expects `react@18` to also be installed. If you have `react@17`, npm throws `ERESOLVE` — it refuses to install because the peer requirement can't be satisfied.

```
npm error ERESOLVE unable to resolve dependency tree
npm error Found: react@17.0.2
npm error Could not resolve dependency:
npm error peer react@"^18.3.1" from react-dom@18.3.1
```

This is npm protecting you from a silent runtime crash. APIs between major React versions are incompatible. npm freezes the pipeline rather than let you ship something that will break.

---

### 4.5 Removing Dependencies

Every dependency you remove:

- reduces the attack surface
- reduces install time
- reduces container image size
- reduces maintenance burden

Before accepting any new package, the question to ask is: do we actually need this, or can we write this ourselves in 10 lines?

---

## 05 · Semantic Versioning

### 5.1 Version Structure

Every npm package version follows the format `MAJOR.MINOR.PATCH`:

| Segment | When it changes | Meaning |
|---|---|---|
| MAJOR | Breaking changes | Existing code may stop working |
| MINOR | New features, backward compatible | Safe to upgrade |
| PATCH | Bug fixes, backward compatible | Safe to upgrade |

---

### 5.2 Version Range Operators

```json
"express": "^5.2.1"   // Caret: allow MINOR and PATCH updates, lock MAJOR
"dotenv":  "~16.0.0"  // Tilde: allow PATCH updates only, lock MAJOR.MINOR
"chalk":   "5.3.0"    // Exact: pin to this version only, no updates ever
"moment":  "latest"   // Latest: whatever the registry considers newest stable
```

**Caret (`^`)** is the npm default. It allows minor and patch upgrades within the same major version. `^5.2.1` can install anything from `5.2.1` onwards but not including `6.0.0`.

**Tilde (`~`)** is more conservative. `~16.0.0` can install anything from `16.0.1` onwards but not including `16.1.0`.

**Exact** is the safest for reproducibility — `npm install chalk@5.3.0` will always install that exact version, nothing newer. `package.json` will record `"chalk": "5.3.0"` — no `^`, no `~`. Any future `npm install` or `npm ci` resolves to exactly `5.3.0`, regardless of what's been published to the registry since.

**Latest** `npm install moment@latest` tells npm to fetch whatever the package maintainer has tagged as the current stable release at the time of install. npm resolves this at install time — so `moment@latest` today might be `2.30.1`, but six months from now the same command could install `2.31.0`. The word `latest` is not locked. Once resolved, `package.json` records the actual version with a caret (`"moment": "^2.30.1"`), and the lockfile pins the exact version that was installed.

---

### 5.3 Version Drift

Version drift happens when `npm install` resolves to a different (usually newer) version than the one previously installed, because the range allows it. This is why lockfiles exist. Without a lockfile, deleting `node_modules` and running `npm install` can result in a different set of packages than what was tested.

---

## 06 · Lockfiles

### 6.1 What package-lock.json Does

`package-lock.json` is the exact record of what was installed. It captures:

- exact version of every package (not just ranges)
- the URL it was downloaded from (`resolved`)
- the SHA-512 integrity hash (`integrity`)
- the full dependency graph

```json
"node_modules/@dabh/diagnostics": {
  "version": "2.0.8",
  "resolved": "https://registry.npmjs.org/@dabh/diagnostics/-/diagnostics-2.0.8.tgz",
  "integrity": "sha512-R4MSXTVnuMzGD7bzHdW2ZhhdPC..."
}
```

When the lockfile exists, `npm install` uses it as the source of truth for exact versions, even if `package.json` would allow something newer.

---

### 6.2 npm install vs npm ci

| | `npm install` | `npm ci` |
|---|---|---|
| Reads | `package.json` first, then lockfile | Lockfile only — ignores `package.json` ranges |
| Updates lockfile | Yes, if newer versions are allowed | Never — treats it as read-only |
| Use case | Development — when you want to install or update | CI/CD — when you need exact reproducibility |
| Speed | Slower (resolves version tree) | Faster (reads exact versions directly) |
| Behavior if lockfile missing | Creates one | Fails with an error |

**Rule of thumb:** developers use `npm install`. CI pipelines use `npm ci`.

The lockfile is what gives you the guarantee that the exact same packages that passed tests in CI are the ones that get built into the production container.

---

### 6.3 What Happens Without a Lockfile

```bash
rm package-lock.json
rm -rf node_modules
npm install
```

npm regenerates the lockfile fresh. It resolves all ranges against the current state of the registry. If any package published a new minor or patch version since you last ran install, you'll get it. In a world where a dependency with a new patch version ships a vulnerability, this is a problem.

**Lesson:** commit your lockfile. Always.

---

## 07 · npm Cache

### 7.1 Cache Architecture

The npm cache lives at `~/.npm/_cacache`. It is a content-addressable store — packages are stored by their SHA-512 hash, not by name. Two packages that happen to share the same content hash (extremely rare but theoretically possible) would share the same cached file.

The cache has two layers:

- `_cacache/content-v2/sha512/` — the actual compressed tarballs
- `_cacache/index-v5/` — the metadata index mapping package names to content hashes

---

### 7.2 Cache Commands

```bash
npm cache verify        # Walk the cache, verify all hashes, garbage-collect orphans
npm cache clean --force # Wipe the entire cache — forces all future installs from network
```

**npm cache verify** output explained:

- `Content verified: 328` — checked 328 cached files, all hashes are intact
- `Content garbage-collected: 1 (95498 bytes)` — found 1 orphaned/corrupted file and removed it
- `Index entries: 328` — metadata index has 328 entries matching the 328 content files

---

### 7.3 Why Cache Matters in DevOps

In CI/CD pipelines, caching the npm cache directory between runs means you don't re-download packages that haven't changed. A pipeline that restores cache before `npm ci` and saves it after can cut install time dramatically. Most CI platforms (GitHub Actions, GitLab CI, Jenkins) have built-in steps for this.

---

## 08 · node_modules Architecture

### 8.1 Why node_modules Exists

Node.js resolves module imports by walking up the directory tree looking for `node_modules`. When code does `require('express')`, Node starts in the current directory, checks for `./node_modules/express`, then goes up one level and checks, then another, until it finds it or reaches the filesystem root.

`node_modules` is the physical location where npm drops all the JavaScript source code for every installed package.

---

### 8.2 Hoisting

When multiple packages need the same dependency at a compatible version, npm installs it once at the root `node_modules` level. This is called hoisting. Both packages can find it by walking up the directory tree. No duplication.

When versions conflict — package A needs lodash `3.x` and package B needs lodash `4.x` — npm can't hoist both to root. It installs the more common version at root and nests the minority version inside the package that needs it:

```
node_modules/
  lodash/           ← version 4.x (hoisted, used by most)
  package-a/
    node_modules/
      lodash/       ← version 3.x (nested, local to package-a)
```

---

### 8.3 .bin — Local Binaries

When a package ships an executable (like `eslint`, `jest`, `nodemon`), npm creates a symlink to it in `./node_modules/.bin`. This is the directory that gets added to PATH when you run `npm run <script>`. It's why you can run `eslint` in a script without installing it globally.

---

### 8.4 Storage Reality

`node_modules` can grow very large very quickly. Installing `aws-sdk` famously adds hundreds of megabytes. This directory should never be committed to version control — it's listed in `.gitignore` and always regenerated from `package-lock.json`.

---

## 09 · Workspaces & Monorepo Engineering

### 9.1 What a Monorepo Is

A monorepo is multiple packages (or applications) living in one Git repository. Instead of separate repos for each service, everything shares one repo, one CI system, one place to manage versions, and one set of tooling.

Large organizations prefer monorepos because:

- services can share code without publishing to the public registry
- version consistency is enforced centrally
- CI pipelines can understand the full dependency graph and know what to rebuild when something changes

---

### 9.2 npm Workspaces

Workspaces are npm's built-in support for monorepos. You declare a root `package.json` that acts as the controller, and it tells npm where to find all the packages:

```json
{
  "name": "node-platform-lab",
  "workspaces": [
    "apps/*",
    "packages/*"
  ]
}
```

`apps/*` — runnable application services (api-gateway, worker-service)
`packages/*` — reusable shared code (logger, config, shared-utils)

---

### 9.3 Internal Packages

An internal package is code written by the team, shared between services, but never published to the public npm registry. It only exists inside the monorepo.

To install an internal package into a workspace:

```bash
npm install @node-lab/logger --workspace api-gateway
```

npm doesn't hit the internet. It finds the package by looking through the declared workspaces, locates `packages/logger`, and links it.

---

### 9.4 Symlinks — How Workspace Linking Works

When you install an internal package, npm creates a symlink in `node_modules` pointing to the actual package source:

```bash
ls -l node_modules/@node-lab/logger
# lrwxrwxrwx → ../../packages/logger
```

The package isn't copied. It's linked. Changes to `packages/logger` are immediately available to any workspace that imports it, without reinstalling. This is the key advantage over copying code between projects.

```bash
ls -l node_modules
# lrwxrwxrwx api-gateway → ../apps/api-gateway
# lrwxrwxrwx worker-service → ../apps/worker-service
```

---

### 9.5 Hoisting in Workspaces

When two packages both depend on `express`, npm installs `express` once at the root `node_modules` and both can access it via the symlink mechanism. This is workspace-level hoisting — it prevents the same package from being duplicated for every workspace that needs it.

When two workspaces need different versions of the same package, npm installs the conflicting version locally inside the workspace's own `node_modules`, leaving the shared version at root for everyone else.

---

### 9.6 Dependency Boundaries

In a properly structured monorepo, the flow of imports goes in one direction only:

```
apps → packages (allowed)
packages → apps (not allowed)
apps → apps (not allowed)
packages → packages (usually not allowed)
```

This prevents circular dependencies and keeps the architecture clean. An `api-gateway` can import from `@node-lab/logger`, but `logger` should never import from `api-gateway`.

---

## 10 · Build Systems & Artifact Generation

### 10.1 What "Building" Means

In a Node.js context, building is the process of transforming source code into a form that can be deployed. The exact transformation depends on the project:

- **TypeScript projects** → transpile `.ts` to `.js`
- **Bundled projects** → combine many files into one optimized file
- **Both** → transpile, then bundle

Node.js cannot execute TypeScript directly. Something must convert it first to Javascript. That conversion is called **transpilation**.

---

### 10.2 TypeScript and Transpilation

TypeScript adds type annotations to JavaScript. Those type annotations are a development-time tool — the compiler checks them, then strips them out. The output is plain JavaScript that Node can run.

```bash
npx tsc --init    # Generates tsconfig.json — the compiler configuration
npx tsc           # Transpiles all .ts files according to tsconfig.json
```

Key `tsconfig.json` fields:

```json
{
  "compilerOptions": {
    "rootDir": "./src",     // Where your TypeScript source files live
    "outDir": "./dist",     // Where compiled JavaScript gets dropped
    "sourceMap": true,      // Generate .map files for debugging
    "declaration": true     // Generate .d.ts type definition files
  }
}
```

---

### 10.3 What Gets Generated

Running `npx tsc` on a single `index.ts` produces:

| File | Purpose |
|---|---|
| `index.js` | The actual executable JavaScript Node runs in production |
| `index.js.map` | Source map — maps lines in `dist/` back to lines in `src/` |
| `index.d.ts` | Type declarations — used by other TypeScript projects that import this one |
| `index.d.ts.map` | Maps type declarations back to original source |

Production deploys `dist/`. Source code lives in `src/`. They serve completely different audiences.

---

### 10.4 Source Maps

Production errors show up as line numbers in compiled JavaScript — which is useless for debugging. Source maps translate those references back to the original TypeScript file and line number.

Without source maps: `Error at dist/index.js:1:4892`
With source maps: `Error at src/utils/math.ts:23:5`

Enable them in `tsconfig.json`: `"sourceMap": true`

---

### 10.5 The dist Folder

`dist/` is the build output. It contains everything that gets shipped to production and nothing that doesn't need to be there:

```bash
npm run build    # Produces dist/
du -sh dist/     # Check output size
ls dist/         # See what was generated
```

`dist/` is never committed to version control. It's in `.gitignore`. The CI pipeline always regenerates it fresh from source during the build stage. This ensures what gets deployed was built from the exact source code in the commit being deployed.

---

### 10.6 Build Failures

When TypeScript compilation fails:

```
src/utils/math.ts:5:2 - error TS1005: ',' expected.
Found 3 errors in the same file.
npm error command failed
npm error code 2
```

Exit code `2` means the build failed. The CI pipeline stops. No artifact gets produced. This is the correct behavior — a broken build should never make it further down the pipeline.

---

### 10.7 esbuild — A Faster Alternative

esbuild is a bundler and transpiler written in Go. It produces smaller output faster than the TypeScript compiler:

| Tool | Build Time | Output Size | Output Files |
|---|---|---|---|
| `tsc` | ~3.3s | 144K | Many (one per source file) |
| `esbuild` | ~1.9s | 8K | One bundled file |

esbuild bundles — it takes all your source files and combines them into a single output file. This is simpler to deploy (one file) and faster to start (one file to load).

For most production Node.js services, esbuild is the better choice. For library packages that need to preserve their module structure for consumers, `tsc` is more appropriate.

---

### 10.8 Bundling

Bundling takes many files and produces one. The bundled file is usually:

- minified (whitespace removed, variable names shortened)
- self-contained (all dependencies included or explicitly excluded)
- optimized for fast loading

```bash
npx esbuild src/index.ts --bundle --platform=node --outdir=dist
```

---

### 10.9 npm Build Scripts

```json
"scripts": {
  "clean": "rm -rf dist",
  "build": "npm run clean && npx tsc",
  "build:esbuild": "npm run clean && npx esbuild src/index.ts --bundle --platform=node --outdir=dist"
}
```

The clean step ensures stale files from previous builds don't mix with fresh output. Always clean before building in CI.

---

## 11 · Testing & Quality Gates

### 11.1 Why Testing Exists

Without tests, the deployment process looks like:

```
Merge → Deploy → Pray
```

Testing replaces prayer with evidence. When a developer changes the logger package, tests are how you know the health endpoint still works. When someone updates a dependency, tests are how you know nothing silently broke.

---

### 11.2 Unit Testing

A unit test validates one function, one behavior, one expectation. Nothing more.

```typescript
// src/utils/statusValidator.ts
export function isValidStatus(status: string): boolean {
  return status === 'UP' || status === 'DOWN';
}

// src/tests/statusValidator.test.ts
import { isValidStatus } from '../utils/statusValidator';

test('returns true for UP', () => {
  expect(isValidStatus('UP')).toBe(true);
});

test('returns false for invalid status', () => {
  expect(isValidStatus('BROKEN')).toBe(false);
});
```

Unit tests are fast (milliseconds each), isolated (no network, no database), and precise (they point to exactly where something broke).

---

### 11.3 Integration Testing

Integration testing checks how multiple layers interact under real conditions — not a single function, but the whole request lifecycle through your server.

```typescript
import request from 'supertest';
import app from '../app';

test('GET /health returns 200 with status UP', async () => {
  const res = await request(app).get('/health');
  expect(res.status).toBe(200);
  expect(res.body.status).toBe('UP');
});
```

This is closer to how the application actually behaves in production: a real HTTP request, real routing, real middleware.

---

### 11.4 Jest

Jest is the standard testing framework for Node.js projects.

```bash
npm install --save-dev jest ts-jest @types/jest
```

- `jest` — the core test runner
- `ts-jest` — transpiles TypeScript before Jest executes it (Jest only runs JavaScript)
- `@types/jest` — type definitions so your editor understands `expect()`, `test()`, etc.

```json
"scripts": {
  "test": "jest",
  "test:coverage": "jest --coverage"
}
```

Jest finds test files by convention — any file matching `*.test.ts` or inside a `__tests__` directory.

---

### 11.5 Test Coverage

Coverage measures how much of your source code is exercised by tests.

```bash
npm test -- --coverage
```

Coverage output:

```
File                | % Stmts | % Branch | % Funcs | % Lines
statusValidator.ts  |   100   |   100    |   100   |   100
```

| Metric | What it measures |
|---|---|
| **Stmts** | Every executable statement run at least once? |
| **Branch** | Both sides of every `if/else` tested? (critical for catching logic bugs) |
| **Funcs** | Every function called at least once? |
| **Lines** | Every line of code hit? |

**Branch coverage is the most important.** If your code has `if (user.isAdmin)` and tests only cover the `true` path, you have 50% branch coverage. The `false` path — the path where non-admin users try to access admin features — is completely untested.

Coverage reports generate an HTML report in `coverage/` that you can open in a browser to see exactly which lines are uncovered.

Organizations enforce coverage thresholds in CI — builds fail if coverage drops below a minimum (commonly 80%). This stops developers from adding new code without adding tests.

---

### 11.6 Static Analysis with ESLint

ESLint reads your code without executing it and finds problems the compiler wouldn't catch:

```bash
npx eslint apps/api-gateway/src/utils/lintSandbox.ts
# 4:1  warning  Unexpected var, use let or const instead
# 4:5  error    'legacySecret' is assigned a value but never used
# 8:9  error    'processingFee' is assigned a value but never used
```

**Key insight:** the application still runs. ESLint found these problems through analysis, not execution. Code that works is not the same as code that's good.

In CI, linting failures block deployment. A variable that's assigned but never used might be an accidentally deleted line — the variable was supposed to be used somewhere. ESLint catches intent mismatches before they cause production incidents.

---

### 11.7 Formatting with Prettier

Prettier is an automatic code formatter. It cares about style — indentation, spacing, quote style, line length — not functionality.

```bash
npm run format    # Rewrites files in place with consistent style
```

Organizations automate formatting for two reasons:

1. Eliminates style debates in code review (nobody argues about tabs vs spaces when a tool decides)
2. Makes diffs meaningful — a code review diff shows only logic changes, not formatting noise

---

### 11.8 The Quality Gate Pipeline

A quality gate is a checkpoint that code must pass before it can proceed to the next stage. A full quality gate looks like:

```bash
npm run build    # Did it compile?
npm test         # Did tests pass? Is coverage above threshold?
npm run lint     # Is the code well-written?
npm run format   # Is the code consistently formatted?
```

If build passes but tests fail: **do not deploy**. Broken behavior shipped is worse than no deployment.
If tests pass but lint fails: **do not deploy**. Lint catches potential bugs, not just style issues.

This is the first mini-CI pipeline. In a real GitHub Actions or Jenkins setup, these four commands are the build stage.

---

## 12 · Security & Supply Chain

### 12.1 The Threat Model

A Node.js application doesn't just run your code. It runs everything in `node_modules`. If any package in that tree has a known vulnerability, your application inherits that risk. If any maintainer pushes a malicious update to a package you depend on, your next `npm install` could introduce an attacker's code into your build.

This is called a **supply chain attack**. It's one of the most serious threats to modern software.

---

### 12.2 How Many Packages Are You Actually Running?

```bash
npm list | wc -l                # Total installed packages
npm ls --depth=0 | grep -c '─'  # Direct dependencies (developer choices)
npm ls --all | grep -c '─'      # All packages including transitive
```

In my lab: 7 direct dependencies, 1,166 total packages. Every one of those 1,159 packages you didn't choose is an implicit trust decision. Do you know who maintains them?

---

### 12.3 npm audit

npm's built-in vulnerability scanner reads your `package-lock.json` and checks every package against the GitHub Advisory Database.

```bash
npm audit
```

Example output:

```
lodash  <=4.17.23
Severity: critical
Prototype Pollution in lodash
Command Injection in lodash
fix available via `npm audit fix`
```

Severity levels:

- **Critical** — an attacker can execute code on your server without credentials. Fix immediately.
- **High** — serious risk, fix before next deployment.
- **Moderate** — evaluate the actual exploit path in your context.
- **Low** — typically minor, informational.

**Important:** don't just run `npm audit fix` blindly. It can update packages to versions with breaking changes. Understand what you're fixing and test afterward.

---

### 12.4 Dependency Graph Investigation

When audit finds a vulnerability, trace where it came from:

```bash
npm ls lodash           # Where in the tree is it?
npm explain lodash      # Who required it and why?
```

If lodash is a direct dependency and it's vulnerable, the fix is straightforward — update it. If it's three levels deep as a transitive dependency of a package you actually need, you need to evaluate: can you update the middle package to a version that pulls in a safe lodash? Can you exclude lodash with the `exclusions` approach? Can you replace the parent package entirely?

---

### 12.5 Trivy — Enterprise Scanner

npm audit only knows about JavaScript packages. Trivy knows about everything.

```bash
trivy fs .    # Scan the filesystem for vulnerabilities
```

| | npm audit | Trivy |
|---|---|---|
| Scope | JavaScript/npm only | Multi-language, multi-ecosystem |
| Infrastructure | Blind to config issues | Detects secrets, misconfigurations |
| Secret detection | No | Yes (tokens, keys in files) |
| Container scanning | No | Yes |
| Data sources | GitHub Advisory Database | NVD, Red Hat, Ubuntu, GitHub, vendor feeds |

Use npm audit in development for quick checks. Use Trivy in CI as part of the security gate.

---

### 12.6 SBOMs — Software Bill of Materials

An SBOM is a formal inventory of every component in your software. Think of it as a cargo manifest for your application — a signed, machine-readable list of exactly what's inside.

```bash
npx @cyclonedx/cyclonedx-npm --output-file security/sbom/bom.json
```

The generated SBOM contains:

- package names and exact versions
- license information for every component
- dependency relationships
- resolved download URLs

```bash
# Query the SBOM to find a specific package's version
jq '.components[] | select(.name == "lodash") | {name, version}' security/sbom/bom.json
```

SBOMs are increasingly required by enterprise customers and governments when purchasing or deploying software. They let anyone audit exactly what your software contains without accessing source code.

---

### 12.7 Supply Chain Risk Assessment

Not all packages deserve equal trust. Before adding a dependency, research:

| Signal | Low Risk | High Risk |
|---|---|---|
| Maintainer | Enterprise-backed (Okta, Google, Microsoft) | Single anonymous maintainer |
| Last update | Active recent updates | Abandoned (years since last commit) |
| Downloads | Millions/week (widely vetted) | Hundreds/week (obscure) |
| Issues | Responsive maintainers | Hundreds of open, unacknowledged issues |
| License | MIT, Apache 2.0, ISC | Unknown, proprietary |

**The lesson from supply chain attacks:** a package can be popular, working, and dangerous at the same time. `event-stream` had 2 million weekly downloads when it was compromised. Popularity is not a security guarantee.

---

### 12.8 Dependency Hygiene

Review `package.json` regularly and ask for every dependency: do we actually need this? Every package removed reduces risk, install time, and maintenance burden. After removing a package:

```bash
npm install
npm test
npm run build
```

If everything passes, it was safe to remove.

---

## 13 · Containerization

### 13.1 Why Containers

The classic problem containers solve: "It works on my laptop but fails in production."

Production servers don't have the same Node version, the same OS libraries, or the same environment configuration as a developer's laptop. Containers package the application and its entire runtime environment into one portable image. Run it anywhere and it behaves identically.

For DevOps, this means:

- build once, deploy anywhere
- no more "works on my machine" incidents
- deployments are reproducible and reversible (roll back by running the previous image)

---

### 13.2 .dockerignore

Before writing a Dockerfile, create `.dockerignore`. It prevents unnecessary files from being included in the build context sent to Docker — which would slow builds and potentially leak secrets.

```
node_modules/
dist/
dist-esbuild/
coverage/
.npm/
*.log
.git
.gitignore
.env
.env.*
.vscode/
.DS_Store
```

`node_modules` should never go into a Docker build. You always reinstall dependencies inside the container from `package-lock.json` so you control exactly what's in there.

`.env` files must never go into images. Environment variables are injected at runtime, not baked into images.

---

### 13.3 Basic Dockerfile

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install --omit=dev
COPY dist/main.js ./dist/
EXPOSE 3000
CMD ["node", "dist/main.js"]
```

Line by line:

- `FROM node:20-alpine` — base image with Node.js on Alpine Linux
- `WORKDIR /app` — all subsequent commands run from here
- `COPY package*.json ./` — copy package files first (important for layer caching)
- `RUN npm install --omit=dev` — install only production dependencies
- `COPY dist/ ./dist/` — copy the compiled application (not source)
- `EXPOSE 3000` — documents the port (doesn't actually open it)
- `CMD ["node", "dist/main.js"]` — what runs when the container starts

---

### 13.4 Image Layers and Caching

Every instruction in a Dockerfile creates a layer. Docker caches layers. If a layer hasn't changed, Docker reuses the cached version instead of rebuilding it.

**This is why you copy `package.json` before copying source code:**

```dockerfile
COPY package*.json ./      # Layer 1: dependencies manifest
RUN npm install            # Layer 2: dependencies installed (slow, cached)
COPY src/ ./src/           # Layer 3: source code (fast, changes often)
RUN npm run build          # Layer 4: build (reruns when source changes)
```

If only source code changes, Docker reuses the cached `npm install` layer. Without this ordering, every source code change would trigger a full `npm install` — potentially hundreds of MB of downloads.

---

### 13.5 Multi-Stage Builds

A multi-stage build separates the build environment from the runtime environment:

```dockerfile
# Stage 1: Build
FROM node:20-alpine AS builder
WORKDIR /build
COPY package*.json ./
RUN npm install
COPY src/ ./src/
COPY tsconfig.json ./
RUN npm run build

# Stage 2: Runtime
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install --omit=dev
COPY --from=builder /build/dist ./dist
EXPOSE 3000
CMD ["node", "dist/server.js"]
```

The final image contains only the runtime stage. The build stage — with its dev tools, TypeScript compiler, and source code — is discarded. The production image has a much smaller attack surface.

| | Single Stage | Multi-Stage |
|---|---|---|
| Image size | Larger (contains build tools) | Smaller (runtime only) |
| Attack surface | Larger (compiler, source code present) | Smaller |
| Source code exposure | Source code in image | Source code not in final image |

---

### 13.6 Base Image Comparison

| Base Image | Size | Use Case |
|---|---|---|
| `node:20` | Large | Full Debian — rarely needed |
| `node:20-slim` | Medium | Debian with unnecessary packages removed |
| `node:20-alpine` | Small | Alpine Linux — good default for most services |
| `node:20-distroless` | Smallest | No shell, no package manager — maximum security |

Alpine is the practical default. It's small, well-maintained, and supported by virtually all npm packages. Distroless is the right choice when security posture is a hard requirement and you don't need shell access inside the container.

---

### 13.7 Environment Variables in Containers

Never bake secrets into images. Inject them at runtime:

```bash
docker run -d \
  -p 3000:3000 \
  -e PORT=3000 \
  -e NODE_ENV=production \
  -e LOG_LEVEL=info \
  --name api-gateway-prod \
  api-gateway:v2
```

Inside the container, `process.env.PORT`, `process.env.NODE_ENV`, and `process.env.LOG_LEVEL` are available. The application reads configuration from the environment, not from files baked into the image.

---

### 13.8 Health Checks

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -qO- http://localhost:3000/health || exit 1
```

A health check tells Docker (and Kubernetes) whether the container is actually functioning, not just running. Container states:

- `starting` — waiting for the initial period to pass
- `healthy` — health check is passing
- `unhealthy` — health check has failed the configured number of times

Load balancers and orchestration systems use health status to route traffic. An unhealthy container gets no traffic. This is why the `/health` endpoint matters — it's not just for developers, it's for the infrastructure.

---

### 13.9 Container Logging

Containers should log to stdout. Docker captures stdout and makes it available through `docker logs`. Log shippers (Promtail, Fluentd) collect from Docker's stdout and ship to centralized storage.

**Why not write logs to files inside containers?**

1. When a container is stopped or replaced, the files are gone. Logs must outlive the container.
2. Files accumulate and can fill disk, crashing the container.
3. Centralized collection from stdout is far simpler to operate.

```bash
docker logs api-gateway-prod            # See all logs
docker logs -f api-gateway-prod         # Follow live
docker logs --tail 50 api-gateway-prod  # Last 50 lines
```

---

### 13.10 Container Lifecycle

```bash
docker build -t api-gateway:v1 .             # Build image
docker run -d --name my-app api-gateway:v1   # Start container
docker ps                                     # Running containers
docker stop my-app                            # Graceful stop (SIGTERM, waits)
docker kill my-app                            # Immediate stop (SIGKILL, no wait)
docker start my-app                           # Start a stopped container
docker restart my-app                         # Stop then start
docker rm my-app                              # Remove container
docker rmi api-gateway:v1                    # Remove image
docker exec -it my-app sh                    # Shell into running container
```

The difference between `stop` and `kill` matters. `stop` sends `SIGTERM` — it gives the application a chance to finish in-flight requests and shut down cleanly. `kill` sends `SIGKILL` — immediate termination, no cleanup. Always `stop` first in production. `kill` is for when `stop` times out.

---

## 14 · Operations & Observability

### 14.1 What Observability Means

Running software and understanding software are different things. A container running is not the same as a container that can be reasoned about. Observability is the ability to answer "what is this system doing, and why?" using the data it produces.

The three pillars of observability:

1. **Logs** — time-stamped records of what happened
2. **Metrics** — numerical measurements over time (latency, error rate, throughput)
3. **Traces** — requests tracked as they move through distributed services

In this phase, the focus is on logs and the pipeline that makes them useful.

---

### 14.2 Structured Logging with Winston

`console.log()` works locally. In production it's useless — no timestamps, no severity levels, no machine-parseable format. Winston is the standard logging library for Node.js production applications.

```typescript
import winston from 'winston';

const logger = winston.createLogger({
  level: process.env.LOG_LEVEL || 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()    // Machine-readable for log aggregators
  ),
  transports: [
    new winston.transports.Console(),           // stdout (for Docker)
    new winston.transports.File({
      filename: 'logs/error.log',
      level: 'error'
    }),
    new winston.transports.File({
      filename: 'logs/application.log'
    })
  ]
});
```

JSON logs look like this:

```json
{"timestamp":"2026-06-10T04:32:33.828Z","level":"info","message":"Health check requested","service":"api-gateway"}
```

Every field is queryable. A log aggregator can filter by `level: "error"` across a million log entries in milliseconds. Plain text logs cannot be processed this way.

---

### 14.3 Log Levels

| Level | Use Case |
|---|---|
| `error` | Something failed — needs immediate attention |
| `warn` | Something unexpected but handled — worth monitoring |
| `info` | Normal operations — service started, request processed |
| `debug` | Detailed diagnostic info — only useful during troubleshooting |
| `silly/trace` | Extremely verbose — almost never used in production |

Production log level is typically `warn` or `error`. Development uses `debug` or `info`. The level is set via environment variable so it changes without rebuilding the container.

---

### 14.4 The Observability Stack: PLG

PLG (Promtail, Loki, Grafana) is a lightweight log management stack well-suited for container environments.

```
Application Container
      ↓ (stdout)
Docker daemon
      ↓
Promtail (log shipper)
      ↓
Loki (log storage)
      ↓
Grafana (visualization + search)
```

**Promtail** watches Docker's container log output, attaches labels (container name, app name, environment), and ships to Loki.

**Loki** stores logs indexed by labels, not by full text content. This makes it much cheaper to operate than Elasticsearch at scale — you can add labels but you can't full-text search across arbitrary fields.

**Grafana** provides the UI. It connects to Loki as a data source and lets you query, filter, and visualize logs. It's also where you build dashboards and set up alerts.

---

### 14.5 Docker Compose for the Observability Stack

```yaml
version: '3.8'
services:
  api-gateway:
    image: api-gateway:latest
    ports: ["3000:3000"]
    environment:
      - PORT=3000
      - NODE_ENV=production
    logging:
      driver: json-file

  loki:
    image: grafana/loki:latest
    ports: ["3100:3100"]
    volumes:
      - ./observability/loki:/etc/loki

  promtail:
    image: grafana/promtail:latest
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./observability/promtail:/etc/promtail
    depends_on: [loki]

  grafana:
    image: grafana/grafana:latest
    ports: ["3001:3000"]
    depends_on: [loki]
```

`/var/run/docker.sock` mounted into Promtail is what gives it access to Docker's log stream. It reads container logs directly from the Docker daemon.

---

### 14.6 Log Retention

Logs cost money. Storage has limits. Retention policy answers: how long do we keep logs before deleting them?

| Environment | Typical retention |
|---|---|
| Development | 24-48 hours |
| Staging | 7-14 days |
| Production | 30-90 days (often regulated) |

Without a retention policy, logs pile up indefinitely. A busy service generating 1GB of logs per day fills 365GB in a year. This is how unconfigured logging crashes services.

---

### 14.7 Investigating a Production Incident

The practical test of observability: a container crashed at 2:13 AM. How do you find out what happened?

1. **Grafana → Explore → Loki** — query logs around 2:10-2:15 AM
2. Filter by `level: "error"` to find the failure event
3. Use the container name label to isolate this specific service's logs
4. Read the stack trace captured in the error log
5. Trace back to what requests were in-flight when it happened

With structured JSON logs, searchable in Loki, visualized in Grafana, this investigation takes minutes. Without this pipeline, you're reading raw `docker logs` output, grepping through files, and guessing.

---

## 15 · The Full Lifecycle — Developer to Production

### 15.1 The Complete Flow

This is the end-to-end view of what happens when a developer says "my code is done":

```
Developer workstation
  └─ npm install          # Install dependencies
  └─ npm run dev          # Local development with hot reload

Source control (Git push)
  └─ Branch created
  └─ Pull request opened
  └─ CI pipeline triggered

CI Pipeline
  ├─ npm ci               # Deterministic install from lockfile
  ├─ npm run build        # Compile TypeScript → JavaScript
  ├─ npm test             # Unit + integration tests
  ├─ npm run lint         # Static analysis
  ├─ npm audit            # Dependency vulnerability scan
  ├─ trivy fs .           # Full filesystem security scan
  ├─ SBOM generation      # Software bill of materials
  └─ docker build         # Package into container image

Container Registry
  └─ Push image (api-gateway:v1.2.3)

Deployment
  └─ Pull image from registry
  └─ docker run (or Kubernetes pod)
  └─ Environment variables injected at runtime
  └─ Health check starts

Operations
  └─ Promtail collecting logs from Docker stdout
  └─ Loki storing and indexing
  └─ Grafana dashboards showing system health
  └─ Alerts firing if error rate spikes
  └─ On-call engineer investigates via Grafana if incident occurs
```

---

### 15.2 What Each Tool Actually Does

| Stage | Tool | What It Contributes |
|---|---|---|
| Dependencies | npm | Manages what code the application uses |
| Versioning | Semantic versioning + lockfile | Reproducible, deterministic installs |
| Build | TypeScript / esbuild | Source code → deployable artifact |
| Test | Jest | Evidence that the code works as intended |
| Quality | ESLint + Prettier | Code that's correct and maintainable |
| Security | npm audit + Trivy | Known vulnerabilities caught before deployment |
| Packaging | Docker + multi-stage build | Application + runtime in one portable image |
| Operations | PLG stack | Running application becomes observable |

---

### 15.3 The DevOps Mindset Shift

Before this journey, "deploying an application" meant handing over code. After it, deploying means being accountable for the entire lifecycle — from dependency choices to how logs are searched during a 2 AM incident.

The tools change between organizations. The pipeline structure changes. But the questions are always the same:

- Is this build reproducible?
- Did tests actually cover the failure paths?
- If this container crashes at 2 AM, will anyone be able to tell why?
- Is there a known vulnerability in this package we're about to ship?

Being able to answer all of those questions confidently is what separates someone who can write Node.js from someone who can operate it.

---
