#!/usr/bin/env python3
# ABOUTME: Analyzes Java files to generate DOT graph of package import dependencies
# ABOUTME: Takes source directories as arguments and outputs relationships between Java packages

#
# Usage example:
#
# python java_package_deps.py pbw-web/src/main/java pbw-lib/src/main/java | dot -Tpng -o dependencies.png
# open dependencies.jpg
#

import os
import sys
import re
import argparse
from pathlib import Path
from collections import defaultdict

def find_java_files(directories):
    """Find all .java files in the given directories."""
    java_files = []
    for directory in directories:
        path = Path(directory)
        if path.exists() and path.is_dir():
            java_files.extend(path.rglob("*.java"))
    return java_files

def extract_package_name(java_file_path):
    """Extract the package name from a Java file."""
    try:
        with open(java_file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()

        # Find package declaration
        package_match = re.search(r'^\s*package\s+([a-zA-Z_][a-zA-Z0-9_.]*)\s*;', content, re.MULTILINE)
        if package_match:
            return package_match.group(1)
    except Exception as e:
        print(f"Warning: Could not read {java_file_path}: {e}", file=sys.stderr)

    return None

def extract_imports(java_file_path):
    """Extract all import statements from a Java file."""
    imports = set()
    try:
        with open(java_file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()

        # Find all import statements
        import_matches = re.findall(r'^\s*import\s+(?:static\s+)?([a-zA-Z_][a-zA-Z0-9_.]*(?:\.\*)?)\s*;', content, re.MULTILINE)

        for import_stmt in import_matches:
            # Remove .* from wildcard imports and get the package
            if import_stmt.endswith('.*'):
                package = import_stmt[:-2]
            else:
                # For specific class imports, get the package (everything before the last dot)
                parts = import_stmt.split('.')
                if len(parts) > 1:
                    package = '.'.join(parts[:-1])
                else:
                    package = import_stmt

            imports.add(package)

    except Exception as e:
        print(f"Warning: Could not read {java_file_path}: {e}", file=sys.stderr)

    return imports

def analyze_java_files(directories):
    """Analyze Java files and build package dependency graph."""
    java_files = find_java_files(directories)
    package_imports = defaultdict(set)
    all_packages = set()

    for java_file in java_files:
        package_name = extract_package_name(java_file)
        if package_name:
            all_packages.add(package_name)
            imports = extract_imports(java_file)

            # Filter out java.lang and other standard library imports if desired
            # and self-imports
            filtered_imports = {imp for imp in imports
                              if imp != package_name and not imp.startswith('java.lang')}

            package_imports[package_name].update(filtered_imports)

    return package_imports, all_packages

def generate_dot_graph(package_imports, all_packages):
    """Generate DOT graph representation of package dependencies."""
    print("digraph PackageDependencies {")
    print("    rankdir=LR;")
    print("    node [shape=box, style=rounded];")
    print("")

    # Add all packages as nodes
    for package in sorted(all_packages):
        # Escape package names for DOT format
        escaped_package = package.replace('.', '_').replace('-', '_')
        print(f'    "{escaped_package}" [label="{package}"];')

    print("")

    # Add edges for dependencies
    edges_added = set()
    for source_package, imported_packages in package_imports.items():
        escaped_source = source_package.replace('.', '_').replace('-', '_')

        for imported_package in imported_packages:
            if imported_package in all_packages:  # Only show internal dependencies
                escaped_imported = imported_package.replace('.', '_').replace('-', '_')
                edge = (escaped_source, escaped_imported)

                if edge not in edges_added:
                    print(f'    "{escaped_source}" -> "{escaped_imported}";')
                    edges_added.add(edge)

    print("}")

def main():
    parser = argparse.ArgumentParser(
        description="Analyze Java files and generate DOT graph of package import dependencies"
    )
    parser.add_argument('directories', nargs='+',
                       help='Source directories to analyze')
    parser.add_argument('--include-stdlib', action='store_true',
                       help='Include standard library imports (java.*, javax.*, etc.)')

    args = parser.parse_args()

    # Check that directories exist
    for directory in args.directories:
        if not os.path.exists(directory):
            print(f"Error: Directory '{directory}' does not exist", file=sys.stderr)
            sys.exit(1)
        if not os.path.isdir(directory):
            print(f"Error: '{directory}' is not a directory", file=sys.stderr)
            sys.exit(1)

    package_imports, all_packages = analyze_java_files(args.directories)

    if not all_packages:
        print("No Java packages found in the specified directories", file=sys.stderr)
        sys.exit(1)

    generate_dot_graph(package_imports, all_packages)

if __name__ == "__main__":
    main()