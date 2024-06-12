import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ItineraryHeaderComponent } from './itinerary-header.component';

describe('ItineraryHeaderComponent', () => {
  let component: ItineraryHeaderComponent;
  let fixture: ComponentFixture<ItineraryHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItineraryHeaderComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ItineraryHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
