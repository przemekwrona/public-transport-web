import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {OtpDatePipe} from "./otp-date.pipe";
import {OtpDurationPipe} from "./otp-duration.pipe";



@NgModule({
  declarations: [
    OtpDatePipe,
    OtpDurationPipe
  ],
  exports: [
    CommonModule,
    OtpDatePipe,
    OtpDurationPipe
  ],
  imports: [
    CommonModule
  ]
})
export class SharedModule { }
