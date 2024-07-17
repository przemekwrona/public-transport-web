import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ItineraryBikeHeaderComponent } from './itinerary-bike-header.component';

describe('ItineraryBikeHeaderComponent', () => {
  let component: ItineraryBikeHeaderComponent;
  let fixture: ComponentFixture<ItineraryBikeHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItineraryBikeHeaderComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ItineraryBikeHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
