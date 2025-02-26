import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BrigadeEditorComponent } from './brigade-editor.component';

describe('BrigadeEditorComponent', () => {
  let component: BrigadeEditorComponent;
  let fixture: ComponentFixture<BrigadeEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BrigadeEditorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(BrigadeEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
