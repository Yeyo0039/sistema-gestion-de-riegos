export function showSection(panel, sectionName) {
    const sections = panel.querySelectorAll("[data-section-panel]");
    const selectedSection = Array.from(sections)
        .find((section) => section.dataset.sectionPanel === sectionName);

    if (!selectedSection) {
        return false;
    }

    sections.forEach((section) => {
        const isSelected = section === selectedSection;
        section.hidden = !isSelected;
        section.setAttribute("aria-hidden", String(!isSelected));
    });

    return true;
}

