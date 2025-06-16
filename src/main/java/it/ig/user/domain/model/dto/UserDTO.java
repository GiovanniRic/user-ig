package it.ig.user.domain.model.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import it.ig.user.domain.model.enums.Role;

import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDTO(Integer id, String name, String surname, String email, String username, String taxCode,
                      Set<Role> roles) {}
