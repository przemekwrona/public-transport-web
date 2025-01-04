import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {AgencyDetails} from "../../generated/public-transport";

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  constructor(private httpClient: HttpClient) { }

  public getAgencyDetails(): Observable<AgencyDetails> {
    return this.httpClient.get<AgencyDetails>('/api/v1/agency')
  }

  public saveAgencyDetails(agencyDetails: AgencyDetails): Observable<void> {
    return this.httpClient.put<void>('/api/v1/agency', agencyDetails)
  }
}
