// Copyright 2018 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.coverageoutputgenerator;

import static com.google.devtools.coverageoutputgenerator.Constants.CC_EXTENSIONS;
import static java.util.Arrays.asList;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

class Coverage {
  private final TreeMap<String, SourceFileCoverage> sourceFiles;

  Coverage() {
    sourceFiles = new TreeMap<>();
  }

  void add(SourceFileCoverage input) throws IncompatibleMergeException {
    String sourceFilename = input.sourceFileName();
    if (sourceFiles.containsKey(sourceFilename)) {
      SourceFileCoverage old = sourceFiles.get(sourceFilename);
      sourceFiles.put(sourceFilename, SourceFileCoverage.merge(old, input));
    } else {
      sourceFiles.put(sourceFilename, input);
    }
  }

  static Coverage merge(Coverage... coverages) throws IncompatibleMergeException {
    return merge(asList(coverages));
  }

  static Coverage merge(List<Coverage> coverages) throws IncompatibleMergeException {
    Coverage merged = new Coverage();
    for (Coverage c : coverages) {
      for (SourceFileCoverage sourceFile : c.getAllSourceFiles()) {
        merged.add(sourceFile);
      }
    }
    return merged;
  }

  static Coverage mergeUnchecked(Coverage... coverages) {
    return mergeUnchecked(asList(coverages));
  }

  static Coverage mergeUnchecked(List<Coverage> coverages) {
    try {
      return merge(coverages);
    } catch (IncompatibleMergeException e) {
      throw new IllegalStateException(e);
    }
  }

  static Coverage create(SourceFileCoverage... sourceFilesCoverage)
      throws IncompatibleMergeException {
    return create(asList(sourceFilesCoverage));
  }

  static Coverage create(List<SourceFileCoverage> sourceFilesCoverage)
      throws IncompatibleMergeException {
    Coverage coverage = new Coverage();
    for (SourceFileCoverage sourceFileCoverage : sourceFilesCoverage) {
      coverage.add(sourceFileCoverage);
    }
    return coverage;
  }

  /**
   * Returns {@link Coverage} only for the given CC source filenames, filtering out every other CC
   * sources of the given coverage. Other types of source files (e.g. Java) will not be filtered
   * out.
   *
   * @param coverage The initial coverage.
   * @param sourcesToKeep The filenames of the sources to keep from the initial coverage.
   */
  static Coverage getOnlyTheseCcSources(Coverage coverage, Set<String> sourcesToKeep) {
    if (coverage == null || sourcesToKeep == null) {
      throw new IllegalArgumentException("Coverage and sourcesToKeep should not be null.");
    }
    if (coverage.isEmpty()) {
      return coverage;
    }
    if (sourcesToKeep.isEmpty()) {
      return new Coverage();
    }
    Coverage finalCoverage = new Coverage();
    for (SourceFileCoverage source : coverage.getAllSourceFiles()) {
      if (!isCcSourceFile(source.sourceFileName())
          || sourcesToKeep.contains(source.sourceFileName())) {
        try {
          finalCoverage.add(source);
        } catch (IncompatibleMergeException e) {
          throw new AssertionError(e);
        }
      }
    }
    return finalCoverage;
  }

  private static boolean isCcSourceFile(String filename) {
    for (String ccExtension : CC_EXTENSIONS) {
      if (filename.endsWith(ccExtension)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Replaces the source file names in the current coverage with their mapping in the given map, if
   * it exists.
   */
  void maybeReplaceSourceFileNames(ImmutableMap<String, String> reportedToOriginalSources) {
    Preconditions.checkNotNull(reportedToOriginalSources);
    if (reportedToOriginalSources.isEmpty()) {
      // nothing to replace
      return;
    }
    for (SourceFileCoverage source : this.getAllSourceFiles()) {
      if (reportedToOriginalSources.containsKey(source.sourceFileName())) {
        source.changeSourcefileName(reportedToOriginalSources.get(source.sourceFileName()));
      }
    }
  }

  static Coverage filterOutMatchingSources(Coverage coverage, List<String> regexes)
      throws IllegalArgumentException {
    if (coverage == null || regexes == null) {
      throw new IllegalArgumentException("Coverage and regex should not be null.");
    }
    if (regexes.isEmpty()) {
      return coverage;
    }
    Coverage filteredCoverage = new Coverage();
    for (SourceFileCoverage source : coverage.getAllSourceFiles()) {
      if (!matchesAnyRegex(source.sourceFileName(), regexes)) {
        try {
          filteredCoverage.add(source);
        } catch (IncompatibleMergeException e) {
          throw new AssertionError(e);
        }
      }
    }
    return filteredCoverage;
  }

  private static boolean matchesAnyRegex(String input, List<String> regexes) {
    for (String regex : regexes) {
      if (input.matches(regex)) {
        return true;
      }
    }
    return false;
  }

  boolean isEmpty() {
    return sourceFiles.isEmpty();
  }

  Collection<SourceFileCoverage> getAllSourceFiles() {
    return sourceFiles.values();
  }
}
