package de.szut.lf8_starter.project;

import de.szut.lf8_starter.hello.HelloEntity;
import de.szut.lf8_starter.hello.dto.HelloCreateDto;
import de.szut.lf8_starter.hello.dto.HelloGetDto;
import de.szut.lf8_starter.project.dtos.AddProjectDto;
import de.szut.lf8_starter.project.dtos.GetProjectDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
