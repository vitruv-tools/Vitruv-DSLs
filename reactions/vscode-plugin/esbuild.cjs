const esbuild = require("esbuild");

const isProduction = process.argv.includes("--production");
const isWatch = process.argv.includes("--watch");

async function main() {
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
