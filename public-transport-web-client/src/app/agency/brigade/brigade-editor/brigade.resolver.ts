import {ResolveFn} from '@angular/router';
import {Observable} from "rxjs";
import {
    BrigadeBodyV2,
    BrigadeService
} from "../../../generated/public-transport-api";
import {inject} from "@angular/core";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const brigadeResolver: ResolveFn<Observable<BrigadeBodyV2>> = (route, state) => {
    const brigadeService: BrigadeService = inject(BrigadeService);
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    const instance: string = agencyStorageService.getInstance();
    const calendarCode: string = route.paramMap.get('calendarCode');
    const calendarSymbol: string = route.paramMap.get('calendarSymbol');

    return brigadeService.getCalendarSymbolBrigadeResources(instance, calendarCode, calendarSymbol);
};
