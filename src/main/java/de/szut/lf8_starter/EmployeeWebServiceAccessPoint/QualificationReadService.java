package de.szut.lf8_starter.EmployeeWebServiceAccessPoint;

import de.szut.lf8_starter.EmployeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import org.springframework.stereotype.Service;

@Service
public class QualificationReadService extends BaseReadService<GetQualificationDto, Long>{
    public QualificationReadService(AccessTokenRetrieverService accessTokenRetrieverService) {
        super("https://employee.szut.dev", accessTokenRetrieverService, GetQualificationDto.class, GetQualificationDto[].class);
    }

    @Override
    protected String getEndpoint() {
        return "qualifications";
    }
}
