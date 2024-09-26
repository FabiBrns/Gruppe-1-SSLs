package de.szut.lf8_starter.project;

import de.szut.lf8_starter.project.dtos.AddProjectDto;
import de.szut.lf8_starter.project.dtos.GetProjectDto;
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

    @GetMapping
    public ResponseEntity<List<GetProjectDto>> getAll() {
        return new ResponseEntity<>(projectService.getAll().stream().map(x-> mappingService.mapProjectEntityToGetProjectDto(x)).toList(), HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<GetProjectDto> getById(@RequestParam Long id) {
        return new ResponseEntity<>(mappingService.mapProjectEntityToGetProjectDto(projectService.getById(id)), HttpStatus.CREATED);
    }
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
