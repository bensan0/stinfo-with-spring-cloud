package com.personal.project.reportservice.controller;

import com.personal.project.commoncore.constants.CommonTerm;
import com.personal.project.commoncore.response.CommonResponse;
import com.personal.project.reportservice.constant.DetailTagEnum;
import com.personal.project.reportservice.model.dto.TagsDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/util")
public class UtilController {

    @GetMapping("/tags")
    public CommonResponse<TagsDTO> getTags() {
        TagsDTO dto = new TagsDTO();
        dto.setPriceSelections(
                List.of(
                        CommonTerm.RISE,
                        CommonTerm.FALL,
                        CommonTerm.UNCHANGED
                )
        );

        dto.setPriceStatusSelections(
                List.of(
                        CommonTerm.RISE,
                        CommonTerm.FALL,
                        CommonTerm.UNCHANGED,
                        CommonTerm.TURN_TO + CommonTerm.RISE,
                        CommonTerm.TURN_TO + CommonTerm.FALL
                )
        );

        dto.setTradingVolumeSelections(
                List.of(
                        CommonTerm.RISE,
                        CommonTerm.FALL,
                        CommonTerm.UNCHANGED
                )
        );

        dto.setTradingAmountSelections(
                List.of(
                        CommonTerm.RISE,
                        CommonTerm.FALL,
                        CommonTerm.UNCHANGED
                )
        );

        dto.setExtraTagsSelections(
                Arrays.stream(DetailTagEnum.values())
                        .map(DetailTagEnum::getTag)
                        .toList()
        );


        return CommonResponse.ok(dto);
    }
}
