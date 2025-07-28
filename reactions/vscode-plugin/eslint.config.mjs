/**
 * ESLint configuration for the project.
 *
 * See https://eslint.style and https://typescript-eslint.io for additional linting options.
 */
// @ts-check
import js from "@eslint/js";
import stylistic from "@stylistic/eslint-plugin";
import globals from "globals";
import tseslint from "typescript-eslint";

export default tseslint.config(
	{
		ignores: ["**/out"],
	},
	js.configs.recommended,
	...tseslint.configs.recommended,
	...tseslint.configs.stylistic,
	{
		plugins: {
			"@stylistic": stylistic,
		},
		rules: {
			curly: "warn",
			"@stylistic/semi": ["warn", "always"],
			"@typescript-eslint/no-empty-function": "off",
			"@typescript-eslint/naming-convention": [
				"warn",
				{
					selector: "import",
					format: ["camelCase", "PascalCase"],
				},
			],
			"@typescript-eslint/no-unused-vars": [
				"error",
				{
					argsIgnorePattern: "^_",
				},
			],
			"@typescript-eslint/no-explicit-any": "off",
		},
	},
	{
		files: ["*.cjs"],
		languageOptions: {
			globals: globals.node,
		},
		rules: {
			"@typescript-eslint/no-require-imports": "off",
		},
	}
);
