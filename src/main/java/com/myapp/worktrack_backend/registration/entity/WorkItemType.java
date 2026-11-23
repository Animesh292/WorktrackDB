package com.myapp.worktrack_backend.registration.entity;

/**
 * Domain types for WorkTrack Work Items (Section 12).
 *
 * Vocabulary:
 *   MISSION       - large business objective (epic-like).
 *   CAPABILITY    - major feature or functional area.
 *   USER_OUTCOME  - user-facing requirement / outcome (story-like).
 *   WORK_ITEM     - concrete unit of development or operational work.
 *   ACTION_STEP   - smaller child unit / subtask.
 *   DEFECT        - bug or faulty behavior.
 *   DISCOVERY     - research, exploration or technical investigation.
 *   ENHANCEMENT   - improvement to existing functionality.
 *   CHECKPOINT    - milestone or delivery target.
 */
public enum WorkItemType {
    MISSION,
    CAPABILITY,
    USER_OUTCOME,
    WORK_ITEM,
    ACTION_STEP,
    DEFECT,
    DISCOVERY,
    ENHANCEMENT,
    CHECKPOINT
}
