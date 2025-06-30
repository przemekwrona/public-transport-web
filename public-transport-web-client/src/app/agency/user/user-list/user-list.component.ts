import { Component } from '@angular/core';
import {DatePipe} from "@angular/common";

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    DatePipe
  ],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css'
})
export class UserListComponent {

}
