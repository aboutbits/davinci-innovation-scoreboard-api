package it.bz.davinci.innovationscoreboard.stats.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class StatsCsvIndexedEvent {
    private Integer fileImportId;
    private String fileName;
}
