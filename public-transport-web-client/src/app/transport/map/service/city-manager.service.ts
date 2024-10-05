import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CityManagerService {

  constructor() { }

  public getCurrentCity(): string {
    return 'WAWA';
  }
}
