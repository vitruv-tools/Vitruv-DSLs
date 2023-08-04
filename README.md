# Vitruv DSLs
[![GitHub Action CI](https://github.com/vitruv-tools/Vitruv-DSLs/actions/workflows/ci.yml/badge.svg)](https://github.com/vitruv-tools/Vitruv-DSLs/actions/workflows/ci.yml)
[![Latest Release](https://img.shields.io/github/release/vitruv-tools/Vitruv-DSLs.svg)](https://github.com/vitruv-tools/Vitruv-DSLs/releases/latest)
[![Issues](https://img.shields.io/github/issues/vitruv-tools/Vitruv-DSLs.svg)](https://github.com/vitruv-tools/Vitruv-DSLs/issues)
[![License](https://img.shields.io/github/license/vitruv-tools/Vitruv-DSLs.svg)](https://raw.githubusercontent.com/vitruv-tools/Vitruv-DSLs/main/LICENSE)

[Vitruvius](https://vitruv.tools) is a framework for view-based (software) development.
It assumes different models to be used for describing a system, which are automatically kept consistent by the framework executing (semi-)automated rules that preserve consistency.
These models are modified only via views, which are projections from the underlying models.
For general information on Vitruvius, see our [GitHub Organisation](https://github.com/vitruv-tools) and our [Wiki](https://github.com/vitruv-tools/.github/wiki).

This DSLs project provides several languages to specify consistency preservation rules in terms of model transformations for keeping models consistent.
Currently, the `Reactions` and the `Commonalities` language are available with different levels of maturity.
The `Reactions` language is the most used and best maintained one.
The DSLs only depend on the [change definition](https://github.com/vitruv-tools/Vitruv-Change) of Vitruvius, such that they can be used standalone to define and execute model transformations.

## Installation

Vitruvius can be installed in Eclipse via the [nightly update site](https://vitruv.tools/updatesite/nightly). A wiki page provides [detailed instructions for using or extending Vitruvius or parts of it](https://github.com/vitruv-tools/.github/wiki/Getting-Started).

## Framework-internal Dependencies

This project depends on the following other projects from the Vitruvius framework:
- [Vitruv-Change](https://github.com/vitruv-tools/Vitruv-Change)
