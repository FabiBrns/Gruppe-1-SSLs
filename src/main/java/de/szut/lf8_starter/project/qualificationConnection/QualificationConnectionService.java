package de.szut.lf8_starter.project.qualificationConnection;

import de.szut.lf8_starter.EmployeeWebServiceAccessPoint.QualificationReadService;
import de.szut.lf8_starter.project.ProjectEntity;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class QualificationConnectionService {
    private final QualificationConnectionRepository qualificationConnectionRepository;
    private QualificationReadService qualificationReadService;

    public QualificationConnectionService(QualificationConnectionRepository qualificationConnectionRepository,
            QualificationReadService qualificationReadService) {
        this.qualificationConnectionRepository = qualificationConnectionRepository;
        this.qualificationReadService = qualificationReadService;
    }

    public Boolean CanAddAllConnectionsToProject(ProjectEntity projectEntity, Set<AddQualificationConnectionDto> qualifications){
        var allIds = new HashSet(qualifications.stream().map(AddQualificationConnectionDto::getQualificationId).toList());
        var allIdsOnServer = new HashSet(Arrays.stream(qualificationReadService.GetAllRequest()).map(x -> x.getId()).toList());
        return allIdsOnServer.containsAll(allIds);
    }

    public void AddAllConnectionsToProject(ProjectEntity projectEntity, Set<AddQualificationConnectionDto> qualifications) {
        var qualificationConnections = new HashSet<>(qualifications.stream().map(AddQualificationConnectionDto::getQualificationId).toList()).stream().map(x -> {
            var entity = new QualificationConnectionEntity();
            entity.setQualificationId(x);
            entity.setProject(projectEntity);
            return entity;
        }).toList();
        qualificationConnectionRepository.saveAll(qualificationConnections);
        projectEntity.setQualificationConnections(qualificationConnections);
    }
}
