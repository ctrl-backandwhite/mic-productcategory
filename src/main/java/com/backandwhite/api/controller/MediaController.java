packagecom.backandwhite.api.controller;importcom.backandwhite.common.constants.AppConstants;importcom.backandwhite.common.security.annotation.NxAdmin;importcom.backandwhite.common.security.annotation.NxPublic;importcom.backandwhite.api.dto.PaginationDtoOut;importcom.backandwhite.api.dto.in.MediaAssetUpdateDtoIn;importcom.backandwhite.api.dto.out.MediaAssetDtoOut;importcom.backandwhite.api.mapper.MediaAssetApiMapper;importcom.backandwhite.api.util.PageableUtils;importcom.backandwhite.application.usecase.MediaAssetUseCase;importcom.backandwhite.domain.model.MediaAsset;importcom.backandwhite.domain.valureobject.MediaCategory;importio.swagger.v3.oas.annotations.Operation;importio.swagger.v3.oas.annotations.Parameter;importio.swagger.v3.oas.annotations.tags.Tag;importjakarta.validation.Valid;importlombok.RequiredArgsConstructor;importorg.springframework.core.io.InputStreamResource;importorg.springframework.data.domain.Page;importorg.springframework.http.HttpHeaders;importorg.springframework.http.HttpStatus;importorg.springframework.http.MediaType;importorg.springframework.http.ResponseEntity;importorg.springframework.web.bind.annotation.*;importorg.springframework.web.multipart.MultipartFile;importjava.io.InputStream;importjava.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/media")
@Tag(name = "Media",description = "Endpointsparagestióndelabibliotecademedios")publicclassMediaController {privatefinalMediaAssetUseCasemediaAssetUseCase;privatefinalMediaAssetApiMappermediaAssetApiMapper;

    // ──Upload ───────────────────────────────────────────────────────────────

    @PostMapping(value = "/upload",consumes =MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subirarchivo",description = "Subeunarchivomultimedia (max10MB).Generathumbnailparaimágenes")publicResponseEntity<MediaAssetDtoOut>upload(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "Archivoasubir") @RequestParam("file")MultipartFilefile,
            @Parameter(description = "Categoríadelasset") @RequestParam(required =false)MediaCategorycategory,
            @Parameter(description = "Textoalternativo") @RequestParam(required =false)Stringalt,
            @Parameter(description = "Etiquetas (comma-separated)") @RequestParam(required =false)List<String>tags) {MediaAssetsaved =mediaAssetUseCase.upload(file,category,alt,tags);returnResponseEntity.status(HttpStatus.CREATED).body(mediaAssetApiMapper.toDto(saved));
    }

    // ──Listado ──────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listarmediaassets",description = "Devuelveassetspaginadosconfiltrosopcionales")publicResponseEntity<PaginationDtoOut<MediaAssetDtoOut>>findAll(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "Filtrarporcategoría") @RequestParam(required =false)MediaCategorycategory,
            @Parameter(description = "FiltrarportipoMIME (parcial)") @RequestParam(required =false)StringmimeType,
            @Parameter(description = "Filtrarporetiqueta") @RequestParam(required =false)Stringtag,
            @Parameter(description = "Númerodepágina",example = "0") @RequestParam(defaultValue = "0")intpage,
            @Parameter(description = "Tamañodepágina",example = "20") @RequestParam(defaultValue = "20")intsize,
            @Parameter(description = "Campodeordenamiento",example = "createdAt") @RequestParam(defaultValue = "createdAt")StringsortBy,
            @Parameter(description = "Ordenascendente",example = "false") @RequestParam(defaultValue = "false")booleanascending) {Page<MediaAsset>result =mediaAssetUseCase.findAll(category,mimeType,tag,page,size,sortBy,ascending);returnResponseEntity.ok(PageableUtils.toResponse(result.map(mediaAssetApiMapper::toDto)));
    }

    // ──Detalle ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "ObtenermediaassetporID")publicResponseEntity<MediaAssetDtoOut>getById(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelasset") @PathVariableStringid) {returnResponseEntity.ok(mediaAssetApiMapper.toDto(mediaAssetUseCase.findById(id)));
    }

    // ──Actualizarmetadata ──────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Actualizarmetadatos",description = "Actualizacategoría,alttextytagsdeunasset")publicResponseEntity<MediaAssetDtoOut>updateMetadata(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @PathVariableStringid,
            @Valid @RequestBodyMediaAssetUpdateDtoIndto) {MediaAssetupdated =mediaAssetUseCase.updateMetadata(id,dto.getCategory(),dto.getAlt(),dto.getTags());returnResponseEntity.ok(mediaAssetApiMapper.toDto(updated));
    }

    // ──Eliminar ─────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminarmediaasset",description = "Eliminaelarchivo,thumbnailyregistrodelaBD")publicResponseEntity<Void>delete(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "IDdelasset") @PathVariableStringid) {mediaAssetUseCase.delete(id);returnResponseEntity.noContent().build();
    }

    // ──Servirimagen ────────────────────────────────────────────────────────

    @GetMapping("/images/{filename}")
    @Operation(summary = "Servirimagen",description = "Sirvelaimagenporfilename (público,cacheable)")publicResponseEntity<InputStreamResource>serveImage(
            @RequestHeader(AppConstants.HEADER_NX036_AUTH)StringnxAuth,
            @Parameter(description = "Nombredelarchivo") @PathVariableStringfilename) {MediaAssetasset =mediaAssetUseCase.findByFilename(filename);InputStreaminputStream =mediaAssetUseCase.loadFile(asset.getFilename());returnResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.getMimeType()))
                .header(HttpHeaders.CACHE_CONTROL, "public,max-age=31536000")
                .body(newInputStreamResource(inputStream));
    }
}
