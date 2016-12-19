package tds.exam.services.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import tds.assessment.Form;
import tds.assessment.Segment;
import tds.exam.services.FormSelector;

@Component
public class RoundRobinFormSelectorImpl implements FormSelector {
    // Keeps track of the index of the next form to assign for a segment
    private Cache<String, FormIndex> formIndexMap;

    public RoundRobinFormSelectorImpl() {
        formIndexMap = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build();
    }

    @Override
    public Optional<Form> selectForm(Segment segment, String languageCode) {
        List<Form> forms = segment.getForms(languageCode);
        int index = 0;
        Optional<Form> selectedForm = Optional.empty();

        if (!forms.isEmpty()) {
            if (forms.size() > 1) { // Round robin multi-form
                index = selectNextIndex(segment.getKey(), forms.size());
            }

            selectedForm = Optional.of(forms.get(index));
        }

        return selectedForm;
    }

    private int selectNextIndex(String segmentKey, int formSize) {
        int index = 0;

        if (!formIndexMap.asMap().containsKey((segmentKey))) {
            formIndexMap.put(segmentKey, new FormIndex());
        } else {
            index = formIndexMap.getIfPresent(segmentKey).getNext(formSize);
        }

        return index;
    }

    private class FormIndex {
        private AtomicInteger currentIndex = new AtomicInteger(0);

        private int getNext(int max) {
            int returnIndex;
            if (currentIndex.get() + 1 == max) {
                currentIndex.set(0);
                returnIndex = currentIndex.get();
            } else {
                returnIndex = currentIndex.incrementAndGet();
            }

            return returnIndex;
        }
    }
}
