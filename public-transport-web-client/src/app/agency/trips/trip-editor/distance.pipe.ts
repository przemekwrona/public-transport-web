import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'distance',
    standalone: false
})
export class DistancePipe implements PipeTransform {

  transform(distanceInMembers: number | undefined): number {
    const value: number = distanceInMembers || 0.0;
    return value / 1000;
  }

}
