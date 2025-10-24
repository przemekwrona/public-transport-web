import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TimetableListComponent} from "./timetable-list/timetable-list.component";



@NgModule({
  imports: [
    CommonModule,
    TimetableListComponent
  ]
})
export class TimetableModule { }
