const esbuild = require("esbuild");
const fs = require("node:fs");
const path = require("node:path");
const { spawnSync } = require("node:child_process");

const isProduction = process.argv.includes("--production");
const isWatch = process.argv.includes("--watch");

const REPO_ROOT = path.resolve(__dirname, "..", "..");
const JAR_NAME = "tools.vitruv.dsls.reactions.ide.jar";
const JAR_SRC = path.join(REPO_ROOT, "reactions", "ide", "target", JAR_NAME);
const JAR_DEST = path.join(__dirname, JAR_NAME);

function buildAndCopyLspJar() {
	if (process.env.REACTIONS_SKIP_LSP_JAR_BUILD === "true") {
		if (!fs.existsSync(JAR_DEST)) {
			throw new Error(
				`REACTIONS_SKIP_LSP_JAR_BUILD is set but ${JAR_NAME} is missing at ${JAR_DEST}. ` +
				`Maybe the CI did not built it successfully.`
			);
		}

		console.log("[lsp-jar] Skipping Maven build");
		return;
	}

	const mvn = path.join(REPO_ROOT, process.platform === "win32" ? "mvnw.cmd" : "mvnw");
	console.log(`[lsp-jar] Building ${JAR_NAME} via Maven...`);
	const result = spawnSync(
		mvn,
		["-pl", "reactions/ide", "-am", "package", "-DskipTests", "-q"],
		{ cwd: REPO_ROOT, stdio: "inherit", shell: true }
	);

	if (result.status !== 0) {
		throw new Error(`Maven build failed with exit code ${result.status}`);
	}

	if (!fs.existsSync(JAR_SRC)) {
		throw new Error(`Expected JAR not found at ${JAR_SRC}`);
	}

	fs.copyFileSync(JAR_SRC, JAR_DEST);
	console.log(`[lsp-jar] Copied to ${path.relative(REPO_ROOT, JAR_DEST)}`);
}

async function main() {
	buildAndCopyLspJar();

	const ctx = await esbuild.context({
		entryPoints: ["src/extension.ts"],
		bundle: true,
		format: "cjs",
		minify: isProduction,
		sourcemap: !isProduction,
		sourcesContent: false,
		platform: "node",
		outfile: "dist/extension.js",
		external: ["vscode"],
	});
	if (isWatch) {
		await ctx.watch();
	} else {
		await ctx.rebuild();
		await ctx.dispose();
	}
}

main().catch((e) => {
	console.error(e);
	process.exit(1);
});
