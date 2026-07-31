package com.visibleai.brasstacks.ai;

import java.util.List;

/**
 * Vendor-neutral AI service interface.
 * Implementations can be swapped without changing business logic.
 */
public interface AiService {

    /**
     * Break a task description into micro-steps.
     * The input MUST be pre-anonymized — no personal data.
     *
     * @param anonymizedTaskDescription task description with personal details removed
     * @return list of step titles
     */
    List<String> decompose(String anonymizedTaskDescription);

    /**
     * Check if the AI service is available and configured.
     */
    boolean isAvailable();
}
