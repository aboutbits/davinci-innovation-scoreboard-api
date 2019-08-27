package it.bz.davinci.innovationscoreboard.stats;

import it.bz.davinci.innovationscoreboard.stats.dto.StatisticsResponseDto;
import it.bz.davinci.innovationscoreboard.stats.dto.StatisticsResponseGroupDto;
import it.bz.davinci.innovationscoreboard.stats.dto.StatisticsResponsePerYearDto;
import it.bz.davinci.innovationscoreboard.stats.es.EmploymentDemographicEs;
import it.bz.davinci.innovationscoreboard.stats.es.EmploymentDemographicEsDao;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
public class EnterprisesWithInnovationAggregator {

    private final EmploymentDemographicEsDao employmentDemographicEsDao;

    public StatisticsResponseDto getEnterprisesWithInnovationActivitiesDividedByTerritory() {
        StatisticsResponseDto result = new StatisticsResponseDto();

        final List<EmploymentDemographicEs> enterprisesWithInnovationActivitiesDividedByTerritory = employmentDemographicEsDao.getEnterprisesWithInnovationActivitiesDividedByTerritory();
        final Map<String, Collection<StatisticsResponsePerYearDto>> statistics = groupByFORMA_INNOVAZ(enterprisesWithInnovationActivitiesDividedByTerritory);

        result.setStatistics(statistics);

        return result;
    }


    public StatisticsResponseDto getEnterprisesWithInnovationActivitiesInItalyDividedByNACE() {
        StatisticsResponseDto result = new StatisticsResponseDto();
        final List<EmploymentDemographicEs> enterprisesWithInnovationActivitiesInItalyDividedByNACE = employmentDemographicEsDao.getEnterprisesWithInnovationActivitiesInItalyDividedByNACE();

        final Map<String, Collection<StatisticsResponsePerYearDto>> statistics = groupByATECO_2007(enterprisesWithInnovationActivitiesInItalyDividedByNACE);

        result.setStatistics(statistics);

        return result;
    }

    public StatisticsResponseDto getEnterprisesThatHaveIntroducedProductOrProcessInnovationsDividedByTerritory() {
        StatisticsResponseDto result = new StatisticsResponseDto();
        final List<EmploymentDemographicEs> enterprisesWithInnovationActivitiesDividedByTerritory = employmentDemographicEsDao.getEnterprisesThatHaveIntroducedProductOrProcessInnovationsDividedByTerritory();
        final Map<String, Collection<StatisticsResponsePerYearDto>> statistics = groupByFORMA_INNOVAZ(enterprisesWithInnovationActivitiesDividedByTerritory);

        result.setStatistics(statistics);

        return result;
    }

    public StatisticsResponseDto getEnterprisesThatHaveIntroducedProductOrProcessInnovationsInItalyDividedByNace() {
        StatisticsResponseDto result = new StatisticsResponseDto();
        final List<EmploymentDemographicEs> enterprisesWithInnovationActivitiesDividedByTerritory = employmentDemographicEsDao.getEnterprisesThatHaveIntroducedProductOrProcessInnovationsInItalyDividedByNace();

        final Map<String, Collection<StatisticsResponsePerYearDto>> statistics = groupByATECO_2007(enterprisesWithInnovationActivitiesDividedByTerritory);

        result.setStatistics(statistics);

        return result;
    }

    public StatisticsResponseDto getInnovationExpenditureDividedByTerritory() {
        StatisticsResponseDto result = new StatisticsResponseDto();
        final List<EmploymentDemographicEs> enterprisesWithInnovationActivitiesDividedByTerritory = employmentDemographicEsDao.getInnovationExpenditureDividedByTerritory();
        final Map<String, Collection<StatisticsResponsePerYearDto>> statistics = groupByFORMA_INNOVAZ(enterprisesWithInnovationActivitiesDividedByTerritory);

        result.setStatistics(statistics);

        return result;
    }

    public StatisticsResponseDto getInnovationExpenditureInItalyDividedByNace() {
        StatisticsResponseDto result = new StatisticsResponseDto();
        final List<EmploymentDemographicEs> enterprisesWithInnovationActivitiesDividedByTerritory = employmentDemographicEsDao.getInnovationExpenditureInItalyDividedByNace();

        final Map<String, Collection<StatisticsResponsePerYearDto>> statistics = groupByATECO_2007(enterprisesWithInnovationActivitiesDividedByTerritory);

        result.setStatistics(statistics);

        return result;
    }

    public StatisticsResponseDto getInnovationExpenditurePerNumberOfPersonsEmployedDividedByTerritory() {
        StatisticsResponseDto result = new StatisticsResponseDto();
        final List<EmploymentDemographicEs> data = employmentDemographicEsDao.getInnovationExpenditurePerNumberOfPersonsEmployedDividedByTerritory();
        final Map<String, Collection<StatisticsResponsePerYearDto>> statistics = groupByFORMA_INNOVAZ(data);

        result.setStatistics(statistics);

        return result;
    }

    public StatisticsResponseDto getInnovationExpenditurePerNumberOfPersonsEmployedInItalyDividedByNace() {
        StatisticsResponseDto result = new StatisticsResponseDto();
        final List<EmploymentDemographicEs> data = employmentDemographicEsDao.getInnovationExpenditurePerNumberOfPersonsEmployedInItalyDividedByNace();

        final Map<String, Collection<StatisticsResponsePerYearDto>> statistics = groupByATECO_2007(data);

        result.setStatistics(statistics);

        return result;
    }

    private Map<String, Collection<StatisticsResponsePerYearDto>> groupByATECO_2007(List<EmploymentDemographicEs> enterprisesWithInnovationActivitiesDividedByTerritory) {
        return enterprisesWithInnovationActivitiesDividedByTerritory.stream()
                .collect(Collectors.groupingBy(
                        EmploymentDemographicEs::getITTER107))
                .entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        row -> {
                            HashMap<String, StatisticsResponsePerYearDto> statisticsPerYear = new HashMap<>();
                            row.getValue().forEach(entry -> {

                                if (isNull(entry.getValue())) {
                                    return;
                                }

                                StatisticsResponsePerYearDto statisticsResponsePerYearDto;
                                if (statisticsPerYear.containsKey(entry.getTIME())) {
                                    statisticsResponsePerYearDto = statisticsPerYear.get(entry.getTIME());
                                } else {
                                    statisticsResponsePerYearDto = new StatisticsResponsePerYearDto();
                                    statisticsResponsePerYearDto.setTotal(BigDecimal.ZERO);
                                    statisticsResponsePerYearDto.setYear(entry.getTIME());
                                    statisticsPerYear.put(entry.getTIME(), statisticsResponsePerYearDto);
                                }

                                final BigDecimal total = statisticsResponsePerYearDto.getTotal().add(entry.getValue());
                                statisticsResponsePerYearDto.setTotal(total);


                                if (isNull(statisticsResponsePerYearDto.getGroups())) {
                                    statisticsResponsePerYearDto.setGroups(new ArrayList<>());
                                }

                                final Optional<StatisticsResponseGroupDto> first = statisticsResponsePerYearDto.getGroups().stream().filter(group -> "ATECO_2007".equals(group.getId())).findFirst();

                                if (first.isPresent()) {
                                    first.get().getValues().put(entry.getATECO_2007(), entry.getValue());
                                } else {
                                    StatisticsResponseGroupDto groupDto = new StatisticsResponseGroupDto();
                                    groupDto.setId("ATECO_2007");
                                    HashMap<String, BigDecimal> values = new HashMap<>();
                                    values.put(entry.getATECO_2007(), entry.getValue());
                                    groupDto.setValues(values);
                                    statisticsResponsePerYearDto.getGroups().add(groupDto);
                                }
                            });
                            return statisticsPerYear.values();
                        }
                ));
    }

    private Map<String, Collection<StatisticsResponsePerYearDto>> groupByFORMA_INNOVAZ(List<EmploymentDemographicEs> enterprisesWithInnovationActivitiesDividedByTerritory) {
        return enterprisesWithInnovationActivitiesDividedByTerritory.stream()
                .collect(Collectors.groupingBy(
                        EmploymentDemographicEs::getITTER107))
                .entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        row -> {
                            HashMap<String, StatisticsResponsePerYearDto> statisticsPerYear = new HashMap<>();
                            row.getValue().forEach(entry -> {

                                StatisticsResponsePerYearDto statisticsResponsePerYearDto;
                                if (statisticsPerYear.containsKey(entry.getTIME())) {
                                    statisticsResponsePerYearDto = statisticsPerYear.get(entry.getTIME());
                                } else {
                                    statisticsResponsePerYearDto = new StatisticsResponsePerYearDto();
                                    statisticsPerYear.put(entry.getTIME(), statisticsResponsePerYearDto);
                                }

                                statisticsResponsePerYearDto.setYear(entry.getTIME());
                                if ("ALL".equals(entry.getFORMA_INNOVAZ())) {
                                    statisticsResponsePerYearDto.setTotal(entry.getValue());
                                } else {
                                    if (isNull(statisticsResponsePerYearDto.getGroups())) {
                                        statisticsResponsePerYearDto.setGroups(new ArrayList<>());
                                    }

                                    final Optional<StatisticsResponseGroupDto> first = statisticsResponsePerYearDto.getGroups().stream().filter(group -> "FORMA_INNOVAZ".equals(group.getId())).findFirst();

                                    if (first.isPresent()) {
                                        first.get().getValues().put(entry.getFORMA_INNOVAZ(), entry.getValue());
                                    } else {
                                        StatisticsResponseGroupDto groupDto = new StatisticsResponseGroupDto();
                                        groupDto.setId("FORMA_INNOVAZ");
                                        HashMap<String, BigDecimal> values = new HashMap<>();
                                        values.put(entry.getFORMA_INNOVAZ(), entry.getValue());
                                        groupDto.setValues(values);
                                        statisticsResponsePerYearDto.getGroups().add(groupDto);
                                    }
                                }

                            });

                            return statisticsPerYear.values();
                        }
                ));
    }


}
