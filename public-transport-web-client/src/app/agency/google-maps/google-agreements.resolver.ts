import {ResolveFn} from '@angular/router';
import {GoogleAgreementsResponse, GoogleAgreementsService} from "../../generated/public-transport-api";
import {inject} from "@angular/core";

export const googleAgreementsResolver: ResolveFn<GoogleAgreementsResponse> = (route, state) => {
    const googleAgreementsService = inject(GoogleAgreementsService);
    return googleAgreementsService.getGoogleAgreements();
};
