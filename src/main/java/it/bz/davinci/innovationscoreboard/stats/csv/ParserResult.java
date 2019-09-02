package it.bz.davinci.innovationscoreboard.stats.csv;

import com.opencsv.exceptions.CsvException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

import static java.util.Objects.isNull;

@AllArgsConstructor
@Data
public class ParserResult<T> {
    private List<T> data;
    private List<CsvException> exceptions;

    public String getExceptionLog() {
        if (isNull(exceptions)) {
            return null;
        }

        return exceptions.stream()
                .map(exception -> "Line: " + exception.getLineNumber() + ", Message: " + exception.getMessage() + "\n")
                .reduce("", String::concat);
    }
}
