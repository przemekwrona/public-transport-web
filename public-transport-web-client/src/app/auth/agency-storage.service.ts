import {Injectable} from '@angular/core';
import {AgencyAddress, AgencyDetails, AgencyService} from "../generated/public-transport-api";
import {map, Observable, of, tap} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class AgencyStorageService {

    static SESSION_STORAGE_INSTANCE_KEY = 'instance';

    private agencyDetails: AgencyDetails = null;

    constructor(private agencyService: AgencyService) {
    }

    public getInstance(): string {
        return sessionStorage.getItem(AgencyStorageService.SESSION_STORAGE_INSTANCE_KEY) || '';
    }

    public setInstance(instance: string): void {
        sessionStorage.setItem(AgencyStorageService.SESSION_STORAGE_INSTANCE_KEY, instance || '');
    }

    public getAgency(): Observable<AgencyDetails> {
        if (this.agencyDetails == null) {
            return this.agencyService.getAgency(this.getInstance())
                .pipe(tap(agencyDetails => this.agencyDetails = agencyDetails));
        } else {
            return of(this.agencyDetails);
        }
    }

    public getAgencyAddress(): Observable<AgencyAddress> {
        return this.getAgency().pipe(map(agencyDetails => agencyDetails.agencyAddress));
    }
}
