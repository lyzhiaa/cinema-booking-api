package kh.edu.istad.moviebooking.features.concession;

import kh.edu.istad.moviebooking.domain.ConcessionItem;
import kh.edu.istad.moviebooking.exception.BadRequestException;
import kh.edu.istad.moviebooking.exception.ResourceNotFoundException;
import kh.edu.istad.moviebooking.features.concession.dto.ConcessionItemResponse;
import kh.edu.istad.moviebooking.features.concession.dto.CreateConcessionRequest;
import kh.edu.istad.moviebooking.features.concession.dto.UpdateConcessionRequest;
import kh.edu.istad.moviebooking.features.file.FileStorageService;
import kh.edu.istad.moviebooking.mapper.ConcessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConcessionServiceImpl implements ConcessionService {

    private final ConcessionItemRepository concessionItemRepository;
    private final ConcessionOrderItemRepository concessionOrderItemRepository;
    private final FileStorageService fileStorageService;

    private final ConcessionMapper concessionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ConcessionItemResponse> getAllConcessions() {

        List<ConcessionItem> concessionItems = concessionItemRepository
                        .findAllByActiveTrueOrderByCreatedAtDesc();

        return concessionMapper.toConcessionItemResponseList(concessionItems);
    }

    @Override
    @Transactional
    public ConcessionItemResponse createConcession(CreateConcessionRequest createConcessionRequest) {

        Optional<ConcessionItem> existingItem = concessionItemRepository
                        .findByNameIgnoreCase(createConcessionRequest.name());

        if (existingItem.isPresent()) {

            ConcessionItem concessionItem = existingItem.get();

            if (Boolean.TRUE.equals(concessionItem.getActive())) {
                throw new BadRequestException("Concession item already exists");
            }

            concessionItem.setName(createConcessionRequest.name());
            concessionItem.setDescription(createConcessionRequest.description());
            concessionItem.setCategory(createConcessionRequest.category());
            concessionItem.setPrice(createConcessionRequest.price());
            concessionItem.setImageUrl(createConcessionRequest.imageUrl());
            concessionItem.setActive(true);

            concessionItemRepository.save(concessionItem);

            return concessionMapper.toConcessionItemResponse(concessionItem);
        }

        ConcessionItem concessionItem = concessionMapper.fromCreateRequest(createConcessionRequest);

        concessionItem.setActive(true);

        concessionItemRepository.save(concessionItem);

        return concessionMapper.toConcessionItemResponse(concessionItem);
    }


    @Override
    @Transactional
    public ConcessionItemResponse updateConcession(
            UUID concessionUuid,
            UpdateConcessionRequest request
    ) {

        ConcessionItem concessionItem = concessionItemRepository
                        .findByUuid(concessionUuid)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "ConcessionItem",
                                        "uuid",
                                        concessionUuid
                                )
                        );

        if (request.name() != null) {
            concessionItem.setName(request.name());
        }

        if (request.description() != null) {
            concessionItem.setDescription(
                    request.description()
            );
        }

        if (request.category() != null) {
            concessionItem.setCategory(
                    request.category()
            );
        }

        if (request.price() != null) {

            if (request.price().signum() <= 0) {
                throw new BadRequestException(
                        "Price must be greater than 0"
                );
            }

            concessionItem.setPrice(
                    request.price()
            );
        }

        if (request.imageUrl() != null) {
            concessionItem.setImageUrl(
                    request.imageUrl()
            );
        }

        concessionItemRepository.save(
                concessionItem
        );

        return concessionMapper
                .toConcessionItemResponse(
                        concessionItem
                );
    }


    @Override
    @Transactional(readOnly = true)
    public ConcessionItemResponse getConcessionByUuid(UUID concessionUuid) {

        ConcessionItem concessionItem = concessionItemRepository
                        .findByUuidAndActiveTrue(concessionUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "ConcessionItem",
                                        "uuid",
                                        concessionUuid
                                )
                        );

        return concessionMapper.toConcessionItemResponse(concessionItem);
    }

    @Override
    @Transactional
    public void deleteConcessionPermanently(UUID concessionUuid) {

        ConcessionItem concessionItem = concessionItemRepository
                        .findByUuid(concessionUuid)
                        .orElseThrow(() -> new ResourceNotFoundException("ConcessionItem","uuid", concessionUuid));

        boolean usedInOrder = concessionOrderItemRepository
                        .existsByConcessionItemUuid(concessionUuid);

        if (usedInOrder) {
            throw new BadRequestException(
                    "Concession item cannot be permanently deleted because it has order history"
            );
        }

        concessionItemRepository.delete(concessionItem);
    }

    @Override
    @Transactional
    public ConcessionItemResponse toggleStatus(UUID concessionUuid) {

        ConcessionItem concessionItem = concessionItemRepository
                        .findByUuid(concessionUuid)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "ConcessionItem",
                                        "uuid",
                                        concessionUuid
                                )
                        );

        concessionItem.setActive(!Boolean.TRUE.equals(concessionItem.getActive()));

        concessionItemRepository.save(concessionItem);

        return concessionMapper.toConcessionItemResponse(concessionItem);
    }

}