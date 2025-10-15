import {ResolveFn} from '@angular/router';
import {BrigadeService} from "../brigade.service";
import {inject} from "@angular/core";
import {Observable} from "rxjs";
import {GetBrigadeResponse} from "../../../generated/public-transport-api";

export const brigadeGetAllResolver: ResolveFn<Observable<GetBrigadeResponse>> = (route, state) => {
    const brigadeService = inject(BrigadeService);

    return brigadeService.getAllBrigades();
};
