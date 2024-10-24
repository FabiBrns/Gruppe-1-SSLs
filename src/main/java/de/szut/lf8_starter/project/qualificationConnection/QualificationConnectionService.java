package de.szut.lf8_starter.project.qualificationConnection;

import de.szut.lf8_starter.employeeWebServiceAccessPoint.Dtos.GetQualificationDto;
import de.szut.lf8_starter.employeeWebServiceAccessPoint.QualificationReadService;
import de.szut.lf8_starter.exceptionHandling.QualificationConflictException;
import de.szut.lf8_starter.exceptionHandling.ResourceNotFoundException;
import de.szut.lf8_starter.project.ProjectEntity;
import de.szut.lf8_starter.project.ProjectRepository;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionDto;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class QualificationConnectionService {
    private final QualificationConnectionRepository qualificationConnectionRepository;
    private final ProjectRepository projectRepository;
    private final QualificationReadService qualificationReadService;

    public QualificationConnectionService(QualificationConnectionRepository qualificationConnectionRepository, ProjectRepository projectRepository, QualificationReadService qualificationReadService) {
        this.qualificationConnectionRepository = qualificationConnectionRepository;
        this.projectRepository = projectRepository;
        this.qualificationReadService = qualificationReadService;
    }

    public void ensureAddAllQualificationConnectionsToProjectIsSafe(ProjectEntity projectEntity, Set<AddQualificationConnectionDto> qualifications) {
        var allIds = new HashSet<>(qualifications.stream().map(AddQualificationConnectionDto::getQualificationId).toList());
        var allIdsOnServer = new HashSet<>(Arrays.stream(qualificationReadService.getAllRequest()).map(GetQualificationDto::getId).toList());

        for (var id : allIds) {
            if (!allIdsOnServer.contains(id))
                throw new ResourceNotFoundException("qualification with id " + id + "does not exist");
        }
    }

    public void addAllConnectionsToProject(ProjectEntity projectEntity, Set<AddQualificationConnectionDto> qualifications) {
        var qualificationConnections = new HashSet<>(qualifications.stream().map(AddQualificationConnectionDto::getQualificationId)
                .toList()).stream().map(x -> {
            var entity = new QualificationConnectionEntity();
            entity.setQualificationId(x);
            entity.setProject(projectEntity);
            return entity;
        }).toList();
        qualificationConnectionRepository.saveAll(qualificationConnections);
        projectEntity.setQualificationConnections(qualificationConnections);
    }

    public void ensureAddQualificationToProjectRequestIsSafe(Long projectId, Long qualificationId) {
        var projectResponse = projectRepository.findById(projectId);
        if (projectResponse.isEmpty())
            throw new ResourceNotFoundException("project with id " + projectId + " does not exist");
        if (Arrays.stream(qualificationReadService.getAllRequest()).noneMatch(x -> Objects.equals(x.getId(), qualificationId)))
            throw new ResourceNotFoundException("qualification with id " + qualificationId + " does not exist");
        if (projectResponse.get().getQualificationConnections().stream().anyMatch(x -> Objects.equals(x.getQualificationId(), qualificationId)))
            throw new QualificationConflictException("qualification with id " + qualificationId + " already exists in the project with id " + projectId);
    }

    public ProjectEntity addQualificationToProject(Long projectId, Long qualificationId) {
        var project = projectRepository.findById(projectId).get();
        var entity = new QualificationConnectionEntity();
        entity.setQualificationId(qualificationId);
        entity.setProject(project);

        qualificationConnectionRepository.save(entity);
        project.getQualificationConnections().add(entity);
        return project;
    }

    public void ensureRemoveQualificationFromProjectRequestIsSafe(Long projectId, Long qualificationId) {
        var projectResponse = projectRepository.findById(projectId);
        if (projectResponse.isEmpty())
            throw new ResourceNotFoundException("project with id " + projectId + " does not exist");
        if (projectResponse.get().getQualificationConnections().stream().filter(x -> Objects.equals(x.getQualificationId(), qualificationId)).toArray().length != 1)
            throw new ResourceNotFoundException("no qualification connection between project with id " + projectId + " and qualification with id " + qualificationId + " exists");

        for (var membership : projectResponse.get().getEmployeeMemberships()) {
            if (Objects.equals(membership.getQualificationId(), qualificationId))
                throw new QualificationConflictException("qualification with id " + qualificationId + " is assigned to employee with id " + membership.getEmployeeId() + " on project with id " + projectId);
        }
    }

    public void removeQualificationFromProject(Long projectId, Long qualificationId) {
        var project = projectRepository.findById(projectId).get();
        var qualificationConnection = project.getQualificationConnections().stream().filter(x -> Objects.equals(x.getQualificationId(), qualificationId)).findFirst().get();
        project.getQualificationConnections().remove(qualificationConnection);
        projectRepository.save(project);
    }
}
