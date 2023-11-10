package com.msik404.karmaappmonolith.user;

import com.msik404.karmaappmonolith.docs.KarmaAppEndpointDocs;
import com.msik404.karmaappmonolith.docs.SwaggerConfiguration;
import com.msik404.karmaappmonolith.docs.UserUpdateRequestWithAdminPrivilegeDoc;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateEmailException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUnexpectedFieldException;
import com.msik404.karmaappmonolith.exception.constraint.exception.DuplicateUsernameException;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithAdminPrivilege;
import com.msik404.karmaappmonolith.user.dto.UserUpdateRequestWithUserPrivilege;
import com.msik404.karmaappmonolith.user.exception.NoFieldSetException;
import com.msik404.karmaappmonolith.user.exception.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = SwaggerConfiguration.AUTH)
public class UserController {

    private final UserService userService;

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_UPDATE_WITH_USER_PRIVILEGE,
            description = KarmaAppEndpointDocs.OP_DESC_UPDATE_WITH_USER_PRIVILEGE
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_ACCOUNT_UPDATE
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = KarmaAppEndpointDocs.RESP_BAD_REQ_ACCOUNT_UPDATE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = UserNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = KarmaAppEndpointDocs.RESP_CONF_NOT_UNIQUE_OBJECT_FIELD,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            )
    })
    @PutMapping("user/users")
    public ResponseEntity<Void> updateWithUserPrivilege(
            @Parameter(description = KarmaAppEndpointDocs.PARAM_DESC_USER_UPDATE_REQUEST_WITH_USER_PRIVILEGE)
            @Valid @RequestBody UserUpdateRequestWithUserPrivilege request
    ) throws DuplicateEmailException, NoFieldSetException, DuplicateUsernameException,
            DuplicateUnexpectedFieldException, UserNotFoundException {

        userService.updateWithUserPrivilege(request);

        return ResponseEntity.ok(null);
    }

    @Operation(
            summary = KarmaAppEndpointDocs.OP_SUM_UPDATE_WITH_ADMIN_PRIVILEGE,
            description = KarmaAppEndpointDocs.OP_DESC_UPDATE_WITH_ADMIN_PRIVILEGE
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = KarmaAppEndpointDocs.RESP_OK_ACCOUNT_UPDATE
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = KarmaAppEndpointDocs.RESP_BAD_REQ_ACCOUNT_UPDATE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = UserNotFoundException.ERROR_MESSAGE,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = KarmaAppEndpointDocs.RESP_CONF_NOT_UNIQUE_OBJECT_FIELD,
                    content = {@Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ProblemDetail.class)
                    )}
            )
    })
    @PutMapping("admin/users/{userId}")
    public ResponseEntity<Void> updateWithAdminPrivilege(

            @Parameter(description = KarmaAppEndpointDocs.PATH_DESC_USER_ID)
            @PathVariable Long userId,

            @Parameter(
                    description = KarmaAppEndpointDocs.PARAM_DESC_USER_UPDATE_REQUEST_WITH_ADMIN_PRIVILEGE,
                    schema = @Schema(
                            name = "UserUpdateRequestWithAdminPrivilege",
                            implementation = UserUpdateRequestWithAdminPrivilegeDoc.class
                    )
            )
            @Valid @RequestBody UserUpdateRequestWithAdminPrivilege request
    ) throws DuplicateEmailException, NoFieldSetException, DuplicateUsernameException,
            DuplicateUnexpectedFieldException, UserNotFoundException {

        userService.updateWithAdminPrivilege(userId, request);

        return ResponseEntity.ok(null);
    }

}
