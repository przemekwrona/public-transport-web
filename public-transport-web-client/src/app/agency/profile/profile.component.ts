import { Component } from '@angular/core';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent {

  public companyName: string = 'NEOBUS POLSKA Czurczak Spółka Komandytowa';
  public agencyUrl: string = 'neobus.pl';
  public timetableUrl: string = '';

}
