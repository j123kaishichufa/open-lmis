/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2013 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.rnr.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.Transformer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openlmis.rnr.domain.Rnr;
import org.openlmis.rnr.domain.RnrLineItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.collect;
import static org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_EMPTY;

@Data
@NoArgsConstructor
@JsonSerialize(include = NON_EMPTY)
public class RnrDTO {

  private Long id;
  private String programName;
  private String programCode;
  private Long programId;
  private String facilityName;
  private String facilityCode;
  private String agentCode;
  private boolean emergency;
  private Date submittedDate;
  private Date modifiedDate;
  private Date periodStartDate;
  private Date periodEndDate;
  private String periodName;
  private Long facilityId;
  private String supplyingDepotName;
  private List<RnrLineItemDTO> products;
  private String status;
  private Long modifiedBy;

  public static List<RnrDTO> prepareForListApproval(List<Rnr> requisitions) {
    List<RnrDTO> result = new ArrayList<>();
    for (Rnr requisition : requisitions) {
      result.add(prepareDTOWithSupplyingDepot(requisition));
    }
    return result;
  }

  public static List<RnrDTO> prepareForView(List<Rnr> requisitions) {
    List<RnrDTO> result = new ArrayList<>();
    for (Rnr requisition : requisitions) {
      RnrDTO rnrDTO = populateDTOWithRequisition(requisition);
      rnrDTO.status = requisition.getStatus().name();
      result.add(rnrDTO);
    }
    return result;
  }

  public static RnrDTO prepareForOrderView(Rnr requisition) {
    RnrDTO rnrDTO = prepareDTOWithSupplyingDepot(requisition);
    rnrDTO.setPeriodName(requisition.getPeriod().getName());
    return rnrDTO;
  }

  private static RnrDTO prepareDTOWithSupplyingDepot(Rnr requisition) {
    RnrDTO rnrDTO = populateDTOWithRequisition(requisition);
    if (requisition.getSupplyingDepot() != null) {
      rnrDTO.supplyingDepotName = requisition.getSupplyingDepot().getName();
    }
    return rnrDTO;
  }

  private static RnrDTO populateDTOWithRequisition(Rnr requisition) {
    RnrDTO rnrDTO = new RnrDTO();
    rnrDTO.id = requisition.getId();
    rnrDTO.programId = requisition.getProgram().getId();
    rnrDTO.facilityId = requisition.getFacility().getId();
    rnrDTO.programName = requisition.getProgram().getName();
    rnrDTO.facilityCode = requisition.getFacility().getCode();
    rnrDTO.facilityName = requisition.getFacility().getName();
    rnrDTO.submittedDate = requisition.getSubmittedDate();
    rnrDTO.modifiedDate = requisition.getModifiedDate();
    rnrDTO.periodStartDate = requisition.getPeriod().getStartDate();
    rnrDTO.periodEndDate = requisition.getPeriod().getEndDate();
    rnrDTO.emergency = requisition.isEmergency();
    return rnrDTO;
  }

  public static RnrDTO prepareForREST(final Rnr rnr) {
    RnrDTO rnrDTO = new RnrDTO();
    rnrDTO.id = rnr.getId();
    rnrDTO.agentCode = rnr.getFacility().getCode();
    rnrDTO.programCode = rnr.getProgram().getCode();
    rnrDTO.periodEndDate = rnr.getPeriod().getEndDate();
    rnrDTO.periodStartDate = rnr.getPeriod().getStartDate();
    rnrDTO.status = rnr.getStatus().name();
    rnrDTO.emergency = rnr.isEmergency();

    ArrayList<RnrLineItem> lineItems = new ArrayList<RnrLineItem>() {{
      addAll(rnr.getNonFullSupplyLineItems());
      addAll(rnr.getFullSupplyLineItems());
    }};

    rnrDTO.products = (List<RnrLineItemDTO>) collect(lineItems, new Transformer() {
      @Override
      public Object transform(Object o) {
        return new RnrLineItemDTO((RnrLineItem) o);
      }
    });
    return rnrDTO;
  }


}
