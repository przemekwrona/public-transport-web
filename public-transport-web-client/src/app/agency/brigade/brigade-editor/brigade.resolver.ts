import {ResolveFn} from '@angular/router';
import {Observable} from "rxjs";
import {BrigadeBody, BrigadePayload, BrigadeService, CalendarSymbolId} from "../../../generated/public-transport-api";
import {inject} from "@angular/core";
import {AgencyStorageService} from "../../../auth/agency-storage.service";

export const brigadeResolver: ResolveFn<Observable<BrigadeBody>> = (route, state) => {
    const brigadeService: BrigadeService = inject(BrigadeService);
    const agencyStorageService: AgencyStorageService = inject(AgencyStorageService);

    const instance: string = agencyStorageService.getInstance();
    const brigadeName: string = route.queryParams['name'];
    const calendarCode: string = route.paramMap.get('calendarCode');
    const calendarSymbol: string = route.paramMap.get('calendarSymbol');

    const calendarSymbolId: CalendarSymbolId = {};
    calendarSymbolId.calendarItemId = {code: calendarCode};
    calendarSymbolId.symbol = calendarSymbol;

    const brigadePayload: BrigadePayload = {};
    brigadePayload.calendarSymbolId = calendarSymbolId;
    brigadePayload.brigadeName = brigadeName;

    return brigadeService.getBrigadeByBrigadeName(instance, brigadePayload);
};
