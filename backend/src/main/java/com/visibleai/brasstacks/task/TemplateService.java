package com.visibleai.brasstacks.task;

import com.visibleai.brasstacks.task.dto.TemplateResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    private static final List<TemplateResponse> TEMPLATES = List.of(
            // Academic
            new TemplateResponse("essay", "Essay", "academic", List.of(
                    "Choose topic and narrow thesis",
                    "Find 3 sources",
                    "Write outline",
                    "Write intro paragraph",
                    "Write body paragraphs",
                    "Write conclusion",
                    "Proofread and edit"
            )),
            new TemplateResponse("exam-prep", "Exam Prep", "academic", List.of(
                    "Gather notes and materials",
                    "Identify key topics",
                    "Create summary sheet",
                    "Do practice problems",
                    "Review weak areas",
                    "Final review pass"
            )),
            new TemplateResponse("homework", "Homework", "academic", List.of(
                    "Read the assignment brief",
                    "Gather materials needed",
                    "Work through problems",
                    "Review answers",
                    "Format and submit"
            )),
            new TemplateResponse("presentation", "Presentation", "academic", List.of(
                    "Define key message",
                    "Outline slide structure",
                    "Create slides",
                    "Add visuals and data",
                    "Write speaker notes",
                    "Practice run-through"
            )),

            // Executive
            new TemplateResponse("board-prep", "Board Prep", "executive", List.of(
                    "Review previous board minutes",
                    "Gather department updates",
                    "Prepare financials summary",
                    "Draft agenda",
                    "Create slide deck",
                    "Rehearse key talking points"
            )),
            new TemplateResponse("quarterly-review", "Quarterly Review", "executive", List.of(
                    "Pull quarterly metrics",
                    "Identify wins and misses",
                    "Draft narrative summary",
                    "Prepare team feedback",
                    "Set next quarter goals",
                    "Schedule review meetings"
            )),
            new TemplateResponse("hiring", "Hiring", "executive", List.of(
                    "Define role requirements",
                    "Write job description",
                    "Post to channels",
                    "Screen applications",
                    "Schedule interviews",
                    "Make decision and offer"
            )),
            new TemplateResponse("strategic-planning", "Strategic Planning", "executive", List.of(
                    "Review current state and metrics",
                    "Identify opportunities and threats",
                    "Define strategic priorities",
                    "Set measurable objectives",
                    "Draft action plan",
                    "Get stakeholder alignment"
            )),

            // Professional
            new TemplateResponse("project-kickoff", "Project Kickoff", "professional", List.of(
                    "Define scope and deliverables",
                    "Identify stakeholders",
                    "Create timeline",
                    "Assign responsibilities",
                    "Set up communication plan",
                    "Schedule kickoff meeting"
            )),
            new TemplateResponse("report", "Report", "professional", List.of(
                    "Define audience and purpose",
                    "Gather data and inputs",
                    "Write draft",
                    "Add charts and visuals",
                    "Review and edit",
                    "Send for feedback"
            )),
            new TemplateResponse("meeting-prep", "Meeting Prep", "professional", List.of(
                    "Define meeting objective",
                    "Create agenda",
                    "Prepare materials",
                    "Send pre-read to attendees",
                    "Run the meeting",
                    "Send follow-up notes"
            ))
    );

    private static final Map<String, TemplateResponse> BY_ID = TEMPLATES.stream()
            .collect(Collectors.toMap(TemplateResponse::id, t -> t));

    public List<TemplateResponse> listTemplates() {
        return TEMPLATES;
    }

    public List<TemplateResponse> listByCategory(String category) {
        return TEMPLATES.stream()
                .filter(t -> t.category().equalsIgnoreCase(category))
                .toList();
    }

    public Optional<TemplateResponse> getTemplate(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }
}
