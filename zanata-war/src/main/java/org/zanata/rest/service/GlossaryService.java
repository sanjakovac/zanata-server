package org.zanata.rest.service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import org.jboss.resteasy.util.GenericType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryLocaleStats;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.rest.dto.LocaleDetails;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GlossaryFileService;
import org.zanata.service.LocaleService;

@Name("glossaryService")
@Path(GlossaryResource.SERVICE_PATH)
@Slf4j
@Transactional
public class GlossaryService implements GlossaryResource {
    @Context
    private UriInfo uri;

    @HeaderParam("Content-Type")
    @Context
    private MediaType requestContentType;

    @Context
    private HttpHeaders headers;

    @Context
    private Request request;

    @In
    private GlossaryDAO glossaryDAO;

    @In
    private GlossaryFileService glossaryFileServiceImpl;

    @In
    private ZanataIdentity identity;

    @In
    private LocaleService localeServiceImpl;

    @Override
    public Response getLocaleStatistic() {
        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }

        List<HLocale> supportedLocales =
            localeServiceImpl.getSupportedAndEnabledLocales();
        Map<LocaleId, Integer> srcLocales = glossaryDAO.getSourceLocales();
        Map<LocaleId, Integer> transLocales =
            glossaryDAO.getTranslationLocales();

        List<GlossaryLocaleStats> result = Lists.newArrayList();

        for(HLocale locale: supportedLocales) {
            LocaleDetails localeDetails = generateLocaleDetails(locale);
            LocaleId localeId = locale.getLocaleId();

            GlossaryLocaleStats localeStats =
                new GlossaryLocaleStats(localeDetails, 0, 0);

            if (srcLocales.containsKey(localeId)) {
                localeStats.setSrcCount(srcLocales.get(localeId));
            }
            if (transLocales.containsKey(localeId)) {
                localeStats.setTransCount(transLocales.get(localeId));
            }

            result.add(localeStats);
        }

        Type genericType = new GenericType<List<GlossaryLocaleStats>>() {
        }.getGenericType();

        Object entity =
            new GenericEntity<List<GlossaryLocaleStats>>(result, genericType);
        return Response.ok(entity).build();
    }

    private LocaleDetails generateLocaleDetails(HLocale locale) {
        return new LocaleDetails(locale.getLocaleId(),
            locale.retrieveDisplayName(), "");
    }

    @Override
    public Response getEntries() {
        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }

        List<HGlossaryEntry> hGlosssaryEntries = glossaryDAO.getEntries();

        Glossary glossary = new Glossary();
        transferEntriesResource(hGlosssaryEntries, glossary);

        return Response.ok(glossary).build();
    }

    @Override
    public Response get(@PathParam("srcLocale") LocaleId srcLocale,
        @PathParam("transLocale") LocaleId transLocale,
        @DefaultValue("-1") @QueryParam("page") int page,
        @DefaultValue("-1") @QueryParam("sizePerPage") int sizePerPage) {

        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }

        int offset = (page - 1) * sizePerPage;
        List<HGlossaryEntry> hGlosssaryEntries =
            glossaryDAO.getEntriesByLocale(srcLocale, transLocale, offset,
                sizePerPage);

        Glossary glossary = new Glossary();

        transferEntriesLocaleResource(hGlosssaryEntries, glossary, transLocale);

        return Response.ok(glossary).build();
    }

    @Override
    public Response put(Glossary glossary) {
        identity.checkPermission("", "glossary-insert");
        ResponseBuilder response;

        // must be a create operation
        response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }
        response = Response.created(uri.getAbsolutePath());

        glossaryFileServiceImpl.saveOrUpdateGlossary(glossary);

        return response.build();
    }

    @Override
    public Response deleteGlossary(LocaleId targetLocale) {
        identity.checkPermission("", "glossary-delete");
        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }

        int rowCount = glossaryDAO.deleteAllEntries(targetLocale);
        log.info("Glossary delete (" + targetLocale + "): " + rowCount);

        return Response.ok().build();
    }

    @Override
    public Response deleteGlossary(LocaleId localeId, String resId) {
        identity.checkPermission("", "glossary-delete");

        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }

        HGlossaryEntry entry =
                glossaryDAO.getEntryByResIdAndLocale(resId, localeId);

        if(entry != null) {
            glossaryDAO.makeTransient(entry);
        }
        return Response.ok().build();
    }

    @Override
    public Response deleteGlossaries() {
        identity.checkPermission("", "glossary-delete");
        ResponseBuilder response = request.evaluatePreconditions();
        if (response != null) {
            return response.build();
        }
        int rowCount = glossaryDAO.deleteAllEntries();
        log.info("Glossary delete all: " + rowCount);

        return Response.ok().build();
    }

    public void transferEntriesResource(List<HGlossaryEntry> hGlossaryEntries,
            Glossary glossary) {
        for (HGlossaryEntry hGlossaryEntry : hGlossaryEntries) {
            GlossaryEntry glossaryEntry = generateGlossaryEntry(hGlossaryEntry);

            for (HGlossaryTerm term : hGlossaryEntry.getGlossaryTerms()
                    .values()) {
                GlossaryTerm glossaryTerm = generateGlossaryTerm(term);
                glossaryEntry.getGlossaryTerms().add(glossaryTerm);
            }
            glossary.getGlossaryEntries().add(glossaryEntry);
        }
    }

    public static void transferEntriesLocaleResource(
            List<HGlossaryEntry> hGlossaryEntries, Glossary glossary,
            LocaleId locale) {
        for (HGlossaryEntry hGlossaryEntry : hGlossaryEntries) {
            GlossaryEntry glossaryEntry = generateGlossaryEntry(hGlossaryEntry);
            LocaleId srcLocale = hGlossaryEntry.getSrcLocale().getLocaleId();

            for (HGlossaryTerm hGlossaryTerm : hGlossaryEntry
                    .getGlossaryTerms().values()) {
                LocaleId termLocale = hGlossaryTerm.getLocale().getLocaleId();
                if (termLocale.equals(locale) || termLocale.equals(srcLocale)) {
                    GlossaryTerm glossaryTerm =
                            generateGlossaryTerm(hGlossaryTerm);

                    glossaryEntry.getGlossaryTerms().add(glossaryTerm);
                }
            }
            glossary.getGlossaryEntries().add(glossaryEntry);
        }
    }

    public static GlossaryEntry generateGlossaryEntry(
            HGlossaryEntry hGlossaryEntry) {
        GlossaryEntry glossaryEntry = new GlossaryEntry(hGlossaryEntry.getResId());
        glossaryEntry.setSrcLang(hGlossaryEntry.getSrcLocale().getLocaleId());
        glossaryEntry.setSourceReference(hGlossaryEntry.getSourceRef());
        glossaryEntry.setPos(hGlossaryEntry.getPos());
        glossaryEntry.setDescription(hGlossaryEntry.getDescription());
        return glossaryEntry;
    }

    public static GlossaryTerm generateGlossaryTerm(HGlossaryTerm hGlossaryTerm) {
        GlossaryTerm glossaryTerm = new GlossaryTerm();
        glossaryTerm.setContent(hGlossaryTerm.getContent());
        glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());

        String name = "";
        if(hGlossaryTerm.getLastModifiedBy() != null) {
            name = hGlossaryTerm.getLastModifiedBy().getName();
        }
        glossaryTerm.setLastModifiedBy(name);
        glossaryTerm.setLastModifiedDate(hGlossaryTerm.getLastChanged());
        glossaryTerm.setComment(hGlossaryTerm.getComment());

        return glossaryTerm;
    }
}
