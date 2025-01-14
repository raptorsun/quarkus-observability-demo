package com.brunobat.micrometer.resource;


import com.brunobat.micrometer.client.FooClient;
import com.brunobat.micrometer.data.LegumeItem;
import com.brunobat.micrometer.data.LegumeNew;
import com.brunobat.micrometer.model.Legume;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.NO_CONTENT;

@ApplicationScoped
@Slf4j
public class LegumeResource implements LegumeApi{

    @Inject
    EntityManager manager;

    @Inject
    Config config;

    @Transactional
    public Response provision() {
        final LegumeNew carrot = LegumeNew.builder()
                .name("Carrot")
                .description("Root vegetable, usually orange")
                .build();
        final LegumeNew zucchini = LegumeNew.builder()
                .name("Zucchini")
                .description("Summer squash")
                .build();
        return Response.status(CREATED).entity(asList(
                addLegume(carrot),
                addLegume(zucchini))).build();
    }

    @Transactional
    public Response add(@Valid final LegumeNew legumeNew) {
        return Response.status(CREATED).entity(addLegume(legumeNew)).build();
    }

    @Transactional
    public Response delete(@NotEmpty final String legumeId) {
        return find(legumeId)
                .map(legume -> {
                    manager.remove(legume);
                    return Response.status(NO_CONTENT).build();
                })
                .orElse(Response.status(NOT_FOUND).build());
    }

    public List<LegumeItem> list() {
        log.info("someone asked for a list");
        return manager.createQuery("SELECT l FROM Legume l").getResultList();
    }

    private Optional<LegumeItem> find(final String legumeId) {
        return Optional.ofNullable(manager.find(Legume.class, legumeId))
                .map(legume -> LegumeItem.builder()
                        .id(legume.getId())
                        .name(legume.getName())
                        .description(legume.getDescription())
                        .build());
    }

    private LegumeItem addLegume(final @Valid LegumeNew legumeNew) {
        final Legume legumeToAdd = Legume.builder()
                .name(legumeNew.getName())
                .description((legumeNew.getDescription()))
                .build();

        final Legume addedLegume = manager.merge(legumeToAdd);

        final LegumeItem legumeItem = LegumeItem.builder()
                .id(addedLegume.getId())
                .name(addedLegume.getName())
                .description(addedLegume.getDescription())
                .build();

        return legumeItem;
    }
}
