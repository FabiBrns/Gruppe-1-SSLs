package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.dtos.AddProjectDto;
import de.szut.lf8_starter.project.dtos.GetProjectDto;
import de.szut.lf8_starter.project.dtos.UpdateProjectDto;
import de.szut.lf8_starter.project.employeeMembership.Dtos.AddEmployeeMembershipDto;
import de.szut.lf8_starter.project.employeeMembership.Dtos.RemoveEmployeeMembershipDto;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.AddQualificationConnectionDto;
import de.szut.lf8_starter.project.qualificationConnection.Dtos.RemoveQualificationConnectionDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "project")
@PreAuthorize("hasAnyAuthority('user')")
public class ProjectController {

    private ProjectService projectService;
    private ProjectMappingService mappingService;

    public ProjectController(ProjectService projectService,
                             ProjectMappingService mappingService) {
        this.projectService = projectService;
        this.mappingService = mappingService;
    }

    @PostMapping("{projectId}/employees")
    public ResponseEntity<GetProjectDto> addEmployeeToProject(@RequestParam Long projectId,  @RequestBody @Valid AddEmployeeMembershipDto addEmployeeMembershipDto) {
        return new ResponseEntity<>(mappingService.mapProjectEntityToGetProjectDto(projectService.AddEmployeeToProject(projectId, addEmployeeMembershipDto)), HttpStatus.CREATED);
    }

    @DeleteMapping("{projectId}/employees")
    public void removeEmployeeToProject(@RequestParam Long projectId, @RequestBody @Valid RemoveEmployeeMembershipDto removeDto) {
        projectService.RemoveEmployeeFromProject(projectId, removeDto.getEmployeeId());
    }

    @PostMapping("{projectId}/qualifications")
    public ResponseEntity<GetProjectDto> addEmployeeToProject(@RequestParam Long projectId,  @RequestBody @Valid AddQualificationConnectionDto addQualificationConnectionDto) {
        return new ResponseEntity<>(mappingService.mapProjectEntityToGetProjectDto(projectService.AddQualificationToProject(projectId, addQualificationConnectionDto)), HttpStatus.CREATED);
    }

    @DeleteMapping("{projectId}/qualifications")
    public void removeQualificationFromProject(@RequestParam Long projectId, @RequestBody @Valid RemoveQualificationConnectionDto removeDto) {
        projectService.RemoveQualificationFromProject(projectId, removeDto.getQualificationId());
    }

    @GetMapping
    public ResponseEntity<List<GetProjectDto>> getAll() {
        return new ResponseEntity<>(projectService.getAll().stream().map(x-> mappingService.mapProjectEntityToGetProjectDto(x)).toList(), HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<GetProjectDto> getById(@RequestParam Long id) {
        return new ResponseEntity<>(mappingService.mapProjectEntityToGetProjectDto(projectService.getById(id)), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetProjectDto> updateById(@RequestParam Long id, @RequestBody @Valid UpdateProjectDto updateProjectDto) {
        return new ResponseEntity<>(mappingService.mapProjectEntityToGetProjectDto(projectService.updateById(id, updateProjectDto)), HttpStatus.CREATED);
    }

    @GetMapping("employee/{id}")
    public ResponseEntity<List<GetProjectDto>> getByEmployeeId(@RequestParam Long id) {
        return new ResponseEntity<>(projectService.getAllByEmployeeId(id).stream().map(x-> mappingService.mapProjectEntityToGetProjectDto(x)).toList(), HttpStatus.CREATED);    }

    @PostMapping
    public ResponseEntity<GetProjectDto> create(@RequestBody @Valid AddProjectDto addProjectDto) throws Exception {
        var createdEntity = projectService.CreateProject(addProjectDto);
        return new ResponseEntity<>(mappingService.mapProjectEntityToGetProjectDto(createdEntity), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public void delete(@RequestParam Long id) {
        projectService.delete(id);
    }
}
