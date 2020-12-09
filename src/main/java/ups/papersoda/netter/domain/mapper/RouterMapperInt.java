package ups.papersoda.netter.domain.mapper;

import ups.papersoda.netter.domain.Router;
import ups.papersoda.netter.dto.RouterDTO;

import java.util.Collection;
import java.util.Map;

public interface RouterMapperInt {
    Map<Long, Router> transformToRouters(Collection<? extends RouterDTO> routerDTOS);
}
