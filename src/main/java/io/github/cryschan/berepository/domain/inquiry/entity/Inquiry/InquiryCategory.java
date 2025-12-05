package io.github.cryschan.berepository.domain.inquiry.entity.Inquiry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryCategory {
    FEATURE, //기능문의
    PAYMENT, //결제및환불
    ACCOUNT, //계정문의
    ETC; //기타

}
