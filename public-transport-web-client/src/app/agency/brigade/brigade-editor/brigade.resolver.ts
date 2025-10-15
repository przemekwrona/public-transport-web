import {ResolveFn} from '@angular/router';
import {Observable} from "rxjs";
import {BrigadeBody} from "../../../generated/public-transport-api";
import {BrigadeService} from "../brigade.service";
import {inject} from "@angular/core";

export const brigadeResolver: ResolveFn<Observable<BrigadeBody>> = (route, state) => {
    const brigadeService: BrigadeService = inject(BrigadeService);

    return brigadeService.getBrigadeByBrigadeName(route.queryParams['name']);
};
