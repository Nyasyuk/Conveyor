package deal.mode;

import deal.enums.EmploymentStatus;
import deal.enums.Position;

import java.math.BigDecimal;

public class Employment {
    private EmploymentStatus employmentStatus;
    private String employmentInn;
    private BigDecimal salary;
    private Position position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}