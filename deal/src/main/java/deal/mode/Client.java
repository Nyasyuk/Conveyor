package deal.mode;

import deal.enums.Gender;
import deal.enums.MaritalStatus;

import java.time.LocalDate;
import java.util.Date;

public class Client {
    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate birthDate;
    private String email;
    private Gender gender;
    private MaritalStatus maritalStatus;
    private Integer dependentAmount;
    private Passport passport;
    private Employment employment;
}
