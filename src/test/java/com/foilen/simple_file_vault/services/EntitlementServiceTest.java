package com.foilen.simple_file_vault.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class EntitlementServiceTest {

    @Test
    void namespaceIn_full_true() {
        Set<String> namespaces = Set.of("example_abc", "example_def");
        Assertions.assertTrue(EntitlementService.namespaceIn("example_abc", namespaces));
        Assertions.assertTrue(EntitlementService.namespaceIn("example_def", namespaces));
    }

    @Test
    void namespaceIn_full_false() {
        Assertions.assertFalse(EntitlementService.namespaceIn("", Set.of("", "example_abc", "example_def")));
        Set<String> namespaces = Set.of("example_abc", "example_def");
        Assertions.assertFalse(EntitlementService.namespaceIn("example_zzz", namespaces));
        Assertions.assertFalse(EntitlementService.namespaceIn("example_aaa", namespaces));
        Assertions.assertFalse(EntitlementService.namespaceIn("example_abcdef", namespaces));
    }

    @Test
    void namespaceIn_wildcard_begin() {
        Set<String> namespaces = Set.of("example_abc", "example_def", "*_dev");
        Assertions.assertTrue(EntitlementService.namespaceIn("example_dev", namespaces));
        Assertions.assertTrue(EntitlementService.namespaceIn("_dev", namespaces));
        Assertions.assertFalse(EntitlementService.namespaceIn("*_dev", namespaces));
    }

    @Test
    void namespaceIn_wildcard_middle() {
        Set<String> namespaces = Set.of("example_abc", "example_def", "example_*_dev");
        Assertions.assertTrue(EntitlementService.namespaceIn("example_dev", namespaces));
        Assertions.assertTrue(EntitlementService.namespaceIn("example_zzz_dev", namespaces));
        Assertions.assertFalse(EntitlementService.namespaceIn("example_dev_", namespaces));
    }

    @Test
    void namespaceIn_wildcard_end() {
        Set<String> namespaces = Set.of("example_*");
        Assertions.assertTrue(EntitlementService.namespaceIn("example_dev", namespaces));
        Assertions.assertTrue(EntitlementService.namespaceIn("example_", namespaces));
        Assertions.assertFalse(EntitlementService.namespaceIn("superb", namespaces));
    }

}