import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AgencyStorageService {

  static SESSION_STORAGE_INSTANCE_KEY = 'instance';

  constructor() { }

  public getInstance(): string {
    return sessionStorage.getItem(AgencyStorageService.SESSION_STORAGE_INSTANCE_KEY) || '';
  }

  public setInstance(instance: string): void {
    sessionStorage.setItem(AgencyStorageService.SESSION_STORAGE_INSTANCE_KEY, instance || '');
  }
}
