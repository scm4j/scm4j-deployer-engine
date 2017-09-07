[![Build Status](https://travis-ci.org/scm4j/scm4j-ai.svg?branch=master)](https://travis-ci.org/scm4j/scm4j-ai)
[![Coverage Status](https://coveralls.io/repos/github/scm4j/scm4j-ai/badge.svg?branch=master)](https://coveralls.io/github/scm4j/scm4j-ai?branch=master)

Status: in development

# Overview
This component automates installation of products which are represented by artifacts in maven repositories. 

# Terms

- `product list`: artifact whose main class implements `IProductList` interface. Describes `products` and maven repositories
- `product`: artifact whose main class implements `IProduct` interface. Describes `components` and their `installation procedures`
- `component`: represented by component artifact, can have one-level dependencies
- `installation procedure`: list of `actions`, every `action` is represented by `installer` class and paremeters. 
- `installer`: class which implements `IInstaller` interface. Is instantiated during `installation procdure`, action paremeters are passed


# Data Structure

Ref. [data-structure.md](data-structure.md)
  
  





